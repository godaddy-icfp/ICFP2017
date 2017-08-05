package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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

public class GameLogic {
  private final ExecutorService executorService;

  private State currentState;

  private ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators =
      ImmutableMap.of(
          Algorithms.AdjacentToMine, AdjacentToMinesAlgorithm::new,
          Algorithms.AdjacentToPath, AdjacentToPathAlgorithm::new);

  // These are constants that value algorithms over all rivers
  // It allows us to select which algorithms are valuable (and which are not) for this particular move
  ImmutableMap<Algorithms, Double> algorithmValues = ImmutableMap.of(
      Algorithms.AdjacentToMine, 1.0,
      Algorithms.AdjacentToPath, 1.0);

  public GameLogic() {
    executorService = Executors.newFixedThreadPool(Algorithms.values().length);
  }

  public SetupP2S setup(final SetupS2P setup) {
    final State state = new State();

    // todo set some more initial state
    state.setPunter(setup.getPunter());

    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);

    final Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> tuple = buildGraph(setup);
    state.setGraph(tuple.right);
    state.setMines(tuple.left);
    state.setShortestPaths(RiverWeightingAlgorithm.apply(tuple.right, tuple.left));

    this.currentState = state;

    return response;
  }

  private River computeWeightOnGraph(State state, ImmutableMap<Algorithms, Double> algorithmValues) {
    // Pick any river
    River bestRiver = null;
    Double bestWeight = 0.0;

    for (River river : state.getGraph().edgeSet()) {
      Double weight = river.getAlgorithmWeights().entrySet().stream()
                           .map(e -> e.getValue() * algorithmValues.get(e.getKey()))
                           .reduce(1.0, (x, y) -> x * y);
      state.getGraph().setEdgeWeight(river, weight);
      if (!river.isClaimed() && (weight > bestWeight)) {
        bestWeight = weight;
        bestRiver = river;
      }
    }
    return bestRiver;
  }

  public static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> buildGraph(final SetupS2P setup) {
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

  public GameplayP2S move(final GameplayS2P move) {
    // load previous state
    final State previousState = Optional.ofNullable(move.getPreviousState()).orElse(this.currentState);

    final State currentState;

    if (previousState != null) {
      currentState = previousState;
    }
    else {
      currentState = new State();
    }

    zeroClaimedEdges(move.getPreviousMoves(), currentState.getGraph(), currentState);

    Pass pass = new Pass();
    pass.setPunter(currentState.getPunter());

    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    runAllAlgorithms(completeLatch, currentState);

    try {
      completeLatch.await(900, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      // ignore so we respond
    }

    // todo initialize a reasonable claim

    // Compute all the weight
    River bestRiver = this.computeWeightOnGraph(currentState, algorithmValues);


    Claim claim = new Claim();
    claim.setPunter(currentState.getPunter());
    claim.setTarget(bestRiver.getTarget());
    claim.setSource(bestRiver.getSource());
    bestRiver.setClaimedBy(currentState.getPunter());

    // initialize the response
    final GameplayP2S response = new GameplayP2S();
    response.setPass(pass); // todo change this to setClaim
    response.setState(currentState);
    response.setClaim(claim);
    currentState.setMoveCount(currentState.getMoveCount() + 1);
    return response;
  }

  private void runAllAlgorithms(final CountDownLatch completeLatch, final State state) {
    algorithmCreators.forEach((algo, creator) -> {
      final GraphAlgorithm graphAlgorithm = creator.create(algo, state);
      executorService.submit(() -> {
        graphAlgorithm.run();
        completeLatch.countDown();
      });
    });
  }

  private void zeroClaimedEdges(
      final PreviousMoves previousMoves,
      final SimpleWeightedGraph<Site, River> map,
      final State state) {

    final List<Move> moves = previousMoves.getMoves();
    moves.stream()
         .filter(m -> m.getClaim() != null)
         .map(m -> m.getClaim())
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

             if (edge != null) {
               map.setEdgeWeight(edge, Weights.Zero);
             }
           }
         });
  }
}
