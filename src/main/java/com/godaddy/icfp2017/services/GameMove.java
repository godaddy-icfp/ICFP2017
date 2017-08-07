package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.Move;
import com.godaddy.icfp2017.models.PreviousMoves;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.google.common.base.Preconditions;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jgrapht.graph.SimpleWeightedGraph;

public class GameMove {

  private final PrintStream debugStream;

  private GameAnalyzer gameAnalyzer;
  private GameAlgorithms gameAlgorithms;
  private GameDecision gameDecision;

  private State initialState;

  public GameMove(State initialState, PrintStream debugStream) {
    this.debugStream = debugStream;
    this.gameAnalyzer = new GameAnalyzer();
    this.gameAlgorithms = new GameAlgorithms(debugStream);
    this.gameDecision = new GameDecision(debugStream);
    this.initialState = initialState;
  }

  public GameplayP2S getMove(final GameplayS2P move) {
    Long startTime = System.currentTimeMillis();

    final State moveState = getState(move);
    gameAnalyzer.run(moveState);
    gameAlgorithms.run(moveState, startTime);
    return gameDecision.getDecision(moveState);

  }

  private State getState(GameplayS2P move) {
    final State currentState = Optional.ofNullable(move.getPreviousState())
        .orElseGet(() -> Optional.ofNullable(this.initialState)
            .orElseThrow(IllegalStateException::new));
    zeroClaimedEdges(move.getPreviousMoves(),
        currentState.getGraph(),
        currentState.getGraphOfEnemyMoves(),
        currentState);
    return currentState;
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
            System.out.println("Add enemy edge " + edge.toString());
            addEnemyEdge(state, graphOfEnemyMoves, sourceVertex, targetVertex, edge,
                claim.getPunter());
          } else {
            // if we own the edge mark it as weight 0, it's free to use
            graph.setEdgeWeight(edge, 0.0);
          }
        });
  }

  private void addEnemyEdge(
      final State state,
      final SimpleWeightedGraph<Site, River> graphOfEnemyMoves,
      final Site sourceVertex,
      final Site targetVertex,
      final River edge,
      final int punter) {
    // Look at source, and for each site
    //  compute the min weight across all edges. This will be the the path closest to the site
    //  this will be the weight for this edge
    //  do this for both source and target

    // Then we have to "extend" the weights from source through target, and from target through source

    Preconditions.checkState(edge.getMaxEnemyPathFromSites().size() == 0);

    graphOfEnemyMoves.edgesOf(sourceVertex).stream().filter(river -> river.getClaimedBy() == punter)
        .forEach(river -> river.getMaxEnemyPathFromSites().forEach((site, weight) -> {
          if (edge.getMaxEnemyPathFromSites().containsKey(site)) {
            if ((weight + 1) < edge.getMaxEnemyPathFromSites().get(site)) {
              edge.getMaxEnemyPathFromSites().put(site, weight + 1);
            }
          } else {
            edge.getMaxEnemyPathFromSites().put(site, weight + 1);
          }
        }));

    graphOfEnemyMoves.edgesOf(targetVertex).stream().filter(river -> river.getClaimedBy() == punter)
        .forEach(river -> river.getMaxEnemyPathFromSites().forEach((site, weight) -> {
          if (edge.getMaxEnemyPathFromSites().containsKey(site)) {
            if ((weight + 1) < edge.getMaxEnemyPathFromSites().get(site)) {
              edge.getMaxEnemyPathFromSites().put(site, weight + 1);
            }
          } else {
            edge.getMaxEnemyPathFromSites().put(site, weight + 1);
          }
        }));

    // Check if the source is a mine
    if (sourceVertex.isMine()) {
      edge.getMaxEnemyPathFromSites().put(sourceVertex, 1);
    }

    // Check if the target is a mine
    if (targetVertex.isMine()) {
      edge.getMaxEnemyPathFromSites().put(targetVertex, 1);
    }

    // Let's propagate

    // TODO:
    // We have to propagate weights collected from rivers connected to source to all rivers connected to target
    // (including source site)
    // And vice versa

    HashMap<Site, Boolean> seen = new HashMap<>();
    dfs(state, graphOfEnemyMoves, edge.getMaxEnemyPathFromSites(), targetVertex, seen,
        edge.getClaimedBy());

    seen = new HashMap<>();
    dfs(state, graphOfEnemyMoves, edge.getMaxEnemyPathFromSites(), sourceVertex, seen,
        edge.getClaimedBy());

    // Add enemy moves, and update the path length
    graphOfEnemyMoves.addEdge(sourceVertex, targetVertex, edge);
  }


  // Given a set of values and a node,
  // go to each connected edge, and copy missing weights
  // recurse
  private void dfs(
      final State state,
      final SimpleWeightedGraph<Site, River> graphOfEnemyMoves,
      final ConcurrentHashMap<Site, Integer> values,
      final Site node,
      final HashMap<Site, Boolean> seen,
      final int punter
  ) {
    seen.put(node, true);

    graphOfEnemyMoves.edgesOf(node).forEach(river -> {
      if (river.getClaimedBy() == punter) {
        // Copy weights over
        values.forEach((site, weight) -> {
          if (!river.getMaxEnemyPathFromSites().containsKey(site)) {
            river.getMaxEnemyPathFromSites().put(site, weight + 1);
          }
        });

        // iterate
        Site target = state.getSiteToMap().get(river.getSource()) == node ?
            state.getSiteToMap().get(river.getTarget()) : node;

        if (!seen.containsKey(target)) {
          dfs(state, graphOfEnemyMoves, river.getMaxEnemyPathFromSites(), target, seen, punter);
        }
      }
    });
  }
}
