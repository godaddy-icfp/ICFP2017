package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.godaddy.icfp2017.services.algorithms.AdjacentToMinesAlgorithm;
import com.godaddy.icfp2017.services.algorithms.AdjacentToPathAlgorithm;
import com.godaddy.icfp2017.services.algorithms.AlgorithmFactory;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.godaddy.icfp2017.services.algorithms.ConnectedDecisionAlgorithm;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import com.godaddy.icfp2017.services.algorithms.MineToMineAlgorithm;
import com.godaddy.icfp2017.services.algorithms.MinimumSpanningTreeAlgorithm;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

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

  private State currentState;

  private final ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators =
      ImmutableMap.of(
          Algorithms.AdjacentToMine, AdjacentToMinesAlgorithm::new,
          Algorithms.AdjacentToPath, AdjacentToPathAlgorithm::new,
          Algorithms.ConnectedDecisionAlgorithm, ConnectedDecisionAlgorithm::new
//          Algorithms.MineToMine, MineToMineAlgorithm::new,
//          Algorithms.MinimumSpanningTree, MinimumSpanningTreeAlgorithm::new
      );

  // These are constants that value algorithms over all rivers
  // It allows us to select which algorithms are valuable (and which are not) for this particular move
  private final ImmutableMap<Algorithms, Double> algorithmValuesMineAcquire = ImmutableMap.of(
      Algorithms.AdjacentToMine, 2.0,
      Algorithms.AdjacentToPath, 0.0,
      Algorithms.ConnectedDecisionAlgorithm, 0.0,
      Algorithms.MineToMine, 3.0,
      Algorithms.MinimumSpanningTree, 2.0);
  private final ImmutableMap<Algorithms, Double> algorithmValuesProgress = ImmutableMap.of(
      Algorithms.AdjacentToMine, 1.0,
      Algorithms.AdjacentToPath, 2.0,
      Algorithms.ConnectedDecisionAlgorithm, 2.0,
      Algorithms.MineToMine, 3.0,
      Algorithms.MinimumSpanningTree, 3.0);

  private ImmutableMap<Algorithms, Double> strategyState = algorithmValuesMineAcquire;

  public GameLogic() {
    executorService = Executors.newFixedThreadPool(Algorithms.values().length);
  }

  public SetupP2S setup(final SetupS2P setup) {
    final State state = new State();

    state.setPunter(setup.getPunter());
    state.setPunters(setup.getPunters());

    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);

    final Triple<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>, SimpleWeightedGraph<Site, River>> triple =
        buildGraphs(
            setup);
    state.setClaimedGraph(triple.right);
    state.setGraph(triple.middle);
    state.setMines(triple.left);
    state.setShortestPaths(new FloydWarshallShortestPaths<>(triple.middle));

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

  private static Triple<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>, SimpleWeightedGraph<Site, River>>
  buildGraphs(
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

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?>
        myClaimedBuilder =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    for (final Site site : sites) {
      site.setMine(mines.contains(site.getId()));
      builder.addVertex(site);
      myClaimedBuilder.addVertex(site);
    }

    for (final River river : rivers) {
      builder.addEdge(
          siteById.get(river.getSource()),
          siteById.get(river.getTarget()),
          river);

      if (river.getClaimedBy() == setup.getPunter()) {
        myClaimedBuilder.addEdge(
            siteById.get(river.getSource()),
            siteById.get(river.getTarget()),
            river);
      }
    }

    return Triple.of(
        mines.stream().map(siteById::get).collect(toImmutableSet()),
        builder.build(),
        myClaimedBuilder.build());
  }

  public GameplayP2S move(final GameplayS2P move,
      Algorithms algorithm) {
    // load previous state

    final State currentState = Optional.ofNullable(move.getPreviousState())
        .orElseGet(() -> Optional.ofNullable(this.currentState)
            .orElseGet(State::new));

    zeroClaimedEdges(move.getPreviousMoves(), currentState.getGraph(), currentState);

    Pass pass = new Pass();
    pass.setPunter(currentState.getPunter());

    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    if (null != algorithm) {
      runSpecificAlgorithm(completeLatch, currentState, algorithm);
    } else {
      runAllAlgorithms(completeLatch, currentState);
    }
    try {
      completeLatch.await(500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      // ignore so we respond
    }

    if (currentState.getMoveCount() * currentState.getPunters() > currentState.getMines().size()) {
      strategyState = algorithmValuesProgress;
    }

    // Compute all the weight
    Optional<River> bestRiver = this.computeWeightOnGraph(currentState, strategyState);

    // initialize the response
    final GameplayP2S response = bestRiver
        .map(river -> {
          final GameplayP2S r = new GameplayP2S();
          Claim claim = new Claim();
          claim.setPunter(currentState.getPunter());
          claim.setTarget(river.getTarget());
          claim.setSource(river.getSource());
          river.setClaimedBy(currentState.getPunter());
          r.setClaim(claim);
          return r;
        })
        .orElseGet(() -> {
          final GameplayP2S r = new GameplayP2S();
          r.setPass(pass);
          return r;
        });

    currentState.setMoveCount(currentState.getMoveCount() + 1);
    response.setState(currentState);

    return response;
  }

  public GraphAlgorithm getGraphAlgorithm(Algorithms algorithm) {
    return algorithmCreators.get(algorithm).create(
          river -> river.getAlgorithmWeights().getOrDefault(algorithm, 1.0),
          (river, score) -> {
            river.getAlgorithmWeights().put(algorithm, score);
            return score;
          });
  }

  private void runSpecificAlgorithm(final CountDownLatch completeLatch, final State state,
      Algorithms algorithm) {
    final GraphAlgorithm graphAlgorithm = getGraphAlgorithm(algorithm);
    executorService.submit(() -> {
      graphAlgorithm.run(algorithm, state);
      completeLatch.countDown();
    });
  }

  private void runAllAlgorithms(final CountDownLatch completeLatch, final State state) {
    for (Algorithms algorithm: algorithmCreators.keySet()) {
      runSpecificAlgorithm(completeLatch, state, algorithm);
    }
  }

  private void zeroClaimedEdges(
      final PreviousMoves previousMoves,
      final SimpleWeightedGraph<Site, River> map,
      final State state) {

    final List<Move> moves = previousMoves.getMoves();
    moves.stream()
        .filter(m -> m.getClaim() != null)
        .map(Move::getClaim)
        .forEach(claim -> {
          if (state.getPunter() != claim.getPunter()) {
            final Site sourceVertex = new Site(claim.getSource());
            final Site targetVertex = new Site(claim.getTarget());

            final River edgeSource = map.getEdge(sourceVertex, targetVertex);

            final River edge = Optional.ofNullable(edgeSource)
                .orElseGet(() -> map.getEdge(targetVertex, sourceVertex));

            if (edge == null) {
              return;
            }

            edge.setClaimedBy(claim.getPunter());
            map.setEdgeWeight(edge, Weights.Max * 1000);
          }
        });
  }
}
