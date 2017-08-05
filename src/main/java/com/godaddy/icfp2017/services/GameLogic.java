package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class GameLogic {
  private State state;

  private final ExecutorService executorService;

  private ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators =
      ImmutableMap.of(
          Algorithms.Adjacent, AdjacentToMinesAlgorithm::new);

  public GameLogic() {
    executorService = Executors.newFixedThreadPool(Algorithms.values().length);
  }

  public SetupP2S setup(final SetupS2P setup) {
    state = new State();

    // todo set some more initial state
    state.setPunter(setup.getPunter());

    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);

    final SimpleWeightedGraph<Site, River> graph = buildGraph(setup);
    state.setMap(graph);

    return response;
  }

  public static SimpleWeightedGraph<Site, River> buildGraph(final SetupS2P setup) {
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

    return builder.build();
  }

  public GameplayP2S move(final GameplayS2P move) {
    // load previous state
    final State previousState = move.getPreviousState();
    if (previousState != null) {
      this.state = previousState;
    }

    zeroClaimedEdges(move.getPreviousMoves(), state.getMap());

    Pass pass = new Pass();
    pass.setPunter(state.getPunter());

    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    runAllAlgorithms(completeLatch);

    try {
      completeLatch.await(900, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      // ignore so we respond
    }

    // todo initialize a reasonable claim
    //    Claim claim = new Claim();
    //    claim.setPunter(state.getPunter());
    //    claim.setTarget(0);
    //    claim.setSource(0);
    //    response.setClaim(claim);

    // initialize the response
    final GameplayP2S response = new GameplayP2S();
    response.setPass(pass); // todo change this to setClaim
    response.setState(state);

    return response;
  }

  private void runAllAlgorithms(final CountDownLatch completeLatch) {
    algorithmCreators.forEach((algo, creator) -> {
      final GraphAlgorithm graphAlgorithm = creator.create(this.state);
      executorService.submit(() -> {
        graphAlgorithm.run();
        completeLatch.countDown();
      });
    });
  }

  private void zeroClaimedEdges(
      final PreviousMoves previousMoves,
      final SimpleWeightedGraph<Site, River> map) {


    final List<Move> moves = previousMoves.getMoves();
    moves.stream()
         .filter(m -> m.getClaim() != null)
         .map(m -> m.getClaim())
         .forEach(claim -> {
           if (state.getPunter() != claim.getPunter()) {
             final River edge = map.getEdge(new Site(claim.getSource()), new Site(claim.getTarget()));
             if (edge != null) {
               map.setEdgeWeight(edge, Weights.Zero);
             }
           }
         });
  }
}
