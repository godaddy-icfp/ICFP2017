package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.godaddy.icfp2017.services.algorithms.*;
import com.godaddy.icfp2017.services.analysis.Analyzers;
import com.godaddy.icfp2017.services.analysis.GraphAnalyzer;
import com.godaddy.icfp2017.services.analysis.MineToMinePathAnalyzer;
import com.godaddy.icfp2017.services.analysis.SiteConnectivityAnalyzer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class GameLogic implements AutoCloseable {
  private final ExecutorService executorService;
  private final PrintStream debugStream;

  private State currentState;

  private final ImmutableMap<Analyzers, GraphAnalyzer> analyzers =
      ImmutableMap.of(
          Analyzers.SiteConnectivity, new SiteConnectivityAnalyzer(),
          Analyzers.MineToMinePath, new MineToMinePathAnalyzer()
      );

  private final ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators =
      ImmutableMap.of(
          Algorithms.AdjacentToMine, AdjacentToMinesAlgorithm::new,
          Algorithms.AdjacentToPath, AdjacentToPathAlgorithm::new,
          Algorithms.ConnectedDecision, ConnectedDecisionAlgorithm::new,
          Algorithms.Connectedness, ConnectednessAlgorithm::new,
          Algorithms.MineToMine, MineToMineAlgorithm::new
          //          Algorithms.MinimumSpanningTree, MinimumSpanningTreeAlgorithm::new
                     );

  // These are constants that value algorithms over all rivers
  // It allows us to select which algorithms are valuable (and which are not) for this particular move
  private final ImmutableMap<Algorithms, Double> algorithmValuesMineAcquire = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 2.0)
      .put(Algorithms.AdjacentToPath, 0.5)
      .put(Algorithms.ConnectedDecision, 0.5)
      .put(Algorithms.Connectedness, 0.5)
      .put(Algorithms.MineToMine, 3.0)
      .put(Algorithms.MinimumSpanningTree, 2.0)
      .build();

  private final ImmutableMap<Algorithms, Double> algorithmValuesProgress = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 1.0)
      .put(Algorithms.AdjacentToPath, 1.0)
      .put(Algorithms.ConnectedDecision, 0.5)
      .put(Algorithms.Connectedness, 1.0)
      .put(Algorithms.MineToMine, 3.0)
      .put(Algorithms.MinimumSpanningTree, 3.0)
      .build();

  private ImmutableMap<Algorithms, Double> strategyState = algorithmValuesMineAcquire;

  public GameLogic(final PrintStream debugStream) {
    this.debugStream = debugStream;
    executorService = Executors.newFixedThreadPool(algorithmCreators.size());
  }

  public SetupP2S setup(final SetupS2P setup) {
    final State state = new State();

    state.setPunter(setup.getPunter());
    state.setPunters(setup.getPunters());

    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);

    final GraphConstruction graphConstruction = buildGraphs(setup);
    state.setGraph(graphConstruction.graph);
    state.setGraphOfEnemyMoves(graphConstruction.graphOfEnemyMoves);
    state.setMines(graphConstruction.mines);
    state.setSiteToMap(graphConstruction.siteToMap);
    state.setShortestPaths(new FloydWarshallShortestPaths<>(graphConstruction.graph));

    new MineToMinePathAnalyzer().analyze(state);
    RankedPathsCalculator calculator = new RankedPathsCalculator(state);
    state.setRankedPaths(calculator.calculate());

    this.currentState = state;

    return response;
  }

  @Override
  public void close() throws Exception {
    executorService.shutdown();
  }

  private Optional<River> computeWeightOnGraph(
      final State state,
      final ImmutableMap<Algorithms, Double> algorithmValues) {
    // Pick any river
    River bestRiver = null;
    double bestWeight = 0.0;

    for (River river : state.getGraph().edgeSet()) {
      double weight = river.getAlgorithmWeights().entrySet().stream()
                           .mapToDouble(e -> e.getValue() * algorithmValues.get(e.getKey()))
                           .reduce(1.0, (x, y) -> x * y);
      state.getGraph().setEdgeWeight(river, weight);
      if (!river.isClaimed() && (weight > bestWeight)) {
        bestWeight = weight;
        bestRiver = river;
      }
    }

    // sometimes we get called and don't produce a river (because they all score 0.0)
    // while we can do a better job, let's just send a pass for now to avoid crashing
    return Optional.ofNullable(bestRiver);
  }

  static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> buildGraph(
      final SetupS2P setup) {
    final Map map = setup.getMap();
    final List<Site> sites = map.getSites();
    final List<River> rivers = map.getRivers();
    final ImmutableSet<Integer> mines = ImmutableSet.copyOf(map.getMines());
    final ImmutableMap<Integer, Site> siteById = sites
        .stream()
        .collect(toImmutableMap(Site::getId, Function.identity()));

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?> builder =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    for (final Site site : sites) {
      site.setMine(mines.contains(site.getId()));
      builder.addVertex(site);
    }

    for (final River river : rivers) {
      builder.addEdge(
          siteById.get(river.getSource()),
          siteById.get(river.getTarget()),
          river);
    }

    return Pair.of(
        mines.stream().map(siteById::get).collect(toImmutableSet()),
        builder.build());
  }

  private static GraphConstruction buildGraphs(final SetupS2P setup) {
    final Map map = setup.getMap();
    final List<Site> sites = map.getSites();
    final List<River> rivers = map.getRivers();
    final ImmutableSet<Integer> mines = ImmutableSet.copyOf(map.getMines());
    final ImmutableMap<Integer, Site> siteById = sites
        .stream()
        .collect(toImmutableMap(Site::getId, Function.identity()));

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?> builder =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?> builderOfEnemyMoves =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    for (final Site site : sites) {
      site.setMine(mines.contains(site.getId()));
      builder.addVertex(site);
      builderOfEnemyMoves.addVertex(site);
    }

    for (final River river : rivers) {
      builder.addEdge(
          siteById.get(river.getSource()),
          siteById.get(river.getTarget()),
          river);
    }

    return new GraphConstruction(
        mines.stream().map(siteById::get).collect(toImmutableSet()),
        builder.build(),
        builderOfEnemyMoves.build(),
        siteById);
  }

  private final Long MAX_TURN_TIME = 900L;
  public GameplayP2S move(
      final GameplayS2P move) {
    // load previous state

    Long startTime = System.currentTimeMillis();

    final State currentState = Optional.ofNullable(move.getPreviousState())
                                       .orElseGet(() -> Optional.ofNullable(this.currentState)
                                                                .orElseThrow(IllegalStateException::new));
    zeroClaimedEdges(move.getPreviousMoves(), currentState.getGraph(), currentState.getGraphOfEnemyMoves(), currentState);

    analyzers.keySet().forEach(key -> {
      analyzers.get(key).run(key.toString(), currentState);
    });

    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    Long timeAvailable = MAX_TURN_TIME - (System.currentTimeMillis() - startTime);
    runAllAlgorithms(completeLatch, currentState);
    try {
      final boolean allCompleted = completeLatch.await(timeAvailable, TimeUnit.MILLISECONDS);
      if (!allCompleted) {
        debugStream.println("Some algorithms didn't finish");
      }
    }
    catch (InterruptedException e) {
      // ignore so we respond
    }

    if (!mineAdjacenciesExist(currentState)) {
      strategyState = algorithmValuesProgress;
    }

    // Compute all the weight
    Optional<River> bestRiver = this.computeWeightOnGraph(currentState, strategyState);

    // initialize the response
    final GameplayP2S response = bestRiver
        .map(river -> {
          final GameplayP2S r = new GameplayP2S();
          final Claim claim = new Claim();
          claim.setPunter(currentState.getPunter());
          claim.setTarget(river.getTarget());
          claim.setSource(river.getSource());
          river.setClaimedBy(currentState.getPunter());
          r.setClaim(claim);
          return r;
        })
        .orElseGet(() -> {
          final GameplayP2S r = new GameplayP2S();
          final Pass pass = new Pass();
          pass.setPunter(currentState.getPunter());
          r.setPass(pass);
          return r;
        });

    currentState.setMoveCount(currentState.getMoveCount() + 1);
    response.setState(currentState);

    return response;
  }

  public boolean isUsingAlgorithm(final Algorithms algorithm) {
    return algorithmCreators.containsKey(algorithm);
  }

  public GraphAlgorithm getGraphAlgorithm(Algorithms algorithm) {
    final AlgorithmFactory algorithmFactory = algorithmCreators.get(algorithm);
    if (null != algorithmFactory) {
      return algorithmFactory.create(
          river -> river.getAlgorithmWeights().getOrDefault(algorithm, Weights.Identity),
          (river, score) -> {
            river.getAlgorithmWeights().put(algorithm, score);
            return score;
          });
    }

    throw new UnsupportedOperationException("No Algorithm creator defined for: " + algorithm);
  }

  private void runSpecificAlgorithm(
      final CountDownLatch completeLatch, final State state,
      Algorithms algorithm) {
    final GraphAlgorithm graphAlgorithm = getGraphAlgorithm(algorithm);
    executorService.submit(() -> {
      try {
        graphAlgorithm.run(algorithm.toString(), state);
      }
      catch (Exception e) {
        debugStream.print(algorithm);
        debugStream.print(" Error: ");
        debugStream.println(e.toString());
      }
      finally {
        completeLatch.countDown();
      }
    });
  }

  private void runAllAlgorithms(final CountDownLatch completeLatch, final State state) {
    for (Algorithms algorithm : algorithmCreators.keySet()) {
      runSpecificAlgorithm(completeLatch, state, algorithm);
    }
  }

  private void zeroClaimedEdges(
      final PreviousMoves previousMoves,
      final SimpleWeightedGraph<Site, River> graph,
      final SimpleWeightedGraph<Site, River> graphOfEnemyMoves,
      final State state) {

    final List<Move> moves = previousMoves.getMoves();
    moves.stream()
         .filter(m -> m.getClaim() != null)
         .map(Move::getClaim)
         .forEach(claim -> {
           final Site sourceVertex = state.getSiteToMap().get(claim.getSource());
           final Site targetVertex = state.getSiteToMap().get(claim.getTarget());
           final River edge = Optional.ofNullable(graph.getEdge(sourceVertex, targetVertex))
               .orElseGet(() -> graph.getEdge(targetVertex, sourceVertex));
           if (edge == null) {
             return;
           }

           edge.setClaimedBy(claim.getPunter());

           if (state.getPunter() != claim.getPunter()) {
             // remove this edge entirely from the graph so we can avoid
             // traversing it during any analysis passes
             graph.removeEdge(edge);

             // but add this edge to the enemy moves
             graphOfEnemyMoves.addEdge(sourceVertex, targetVertex, edge);
             System.out.println("Add enemy edge " + edge.toString());
           } else {
             // if we own the edge mark it as weight 0, it's free to use
             graph.setEdgeWeight(edge, 0.0);
           }
         });
  }

  private boolean mineAdjacenciesExist(State state) {
    long mineAdjacencyCount = state
        .getMines()
        .stream()
        .flatMap(mine -> state.getGraph()
                              .edgesOf(mine)
                              .stream()
                              .filter(river -> !river.isClaimed()))
        .count();

    return mineAdjacencyCount > 0;
  }
}
