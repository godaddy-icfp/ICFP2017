package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashMap;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

public class EnemyPathWeightAlgorithm extends BaseAlgorithm {
  public EnemyPathWeightAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    final SimpleWeightedGraph<Site, River> graphOfEnemyMoves = state.getGraphOfEnemyMoves();
    final double siteExp = graph.vertexSet().size() * graph.vertexSet().size();

    graph.vertexSet().forEach(site -> {

      if (site.isMine()) return;

      double totalEdges = 0.0;
      double takenEdges = 0.0;

      // We take the minimum from each site (just like the weighing algorithm)
      HashMap<Site, Integer> distances = new HashMap<>();

      // Merge from graph and enemy graph
      ImmutableSet<River> mergedRivers = ImmutableSet.<River>builder().addAll(graph.edgesOf(site)).addAll(graphOfEnemyMoves.edgesOf(site)).build();

      for (River river : mergedRivers) {
        if (river.isClaimed() && river.getClaimedBy() != state.getPunter()) takenEdges += 1;
        totalEdges += 1;

        river.getMaxEnemyPathFromSites().forEach((mine, weight) -> {
          if (distances.containsKey(mine)) {
            // min
            if (weight < distances.get(mine)) {
              distances.put(mine, weight);
            }
          } else {
            distances.put(mine, weight);
          }
        });
      }

      if (takenEdges > 0) {
        // Compute the max of the distances
        double maxDistance = distances.values().stream().max(Comparator.naturalOrder()).orElse(0);
        maxDistance *= maxDistance;

        // Weight computation
        double weight = 0.5 + (
            0.5 * (maxDistance / siteExp) * (takenEdges / totalEdges)
        );

        // Set the weight on all outgoing edges
        // Compute the shortest path from the edge targets to any mine, and favor the ones that
        // have a shorter path

        final DijkstraShortestPath<Site, River> shortestPath = new DijkstraShortestPath<>(graph);
        final ImmutableSet<SingleSourcePaths<Site, River>> shortestMinePaths = state
            .getMines()
            .stream()
            .map(shortestPath::getPaths)
            .collect(ImmutableSet.toImmutableSet());

        // Compute the shortest paths from each of the possible vertices
        int minShortestPath = Integer.MAX_VALUE;
        for (River river : graph.edgesOf(site)) {
          if (!river.isClaimed()) {

            // Find the river target, and update their shortest paths
            // TODO: if we have a previous one, should we recompute?
            Site targetVertex = state.getSiteToMap().get(river.getSource()) == site ?
                state.getSiteToMap().get(river.getTarget()) : state.getSiteToMap().get(river.getSource());

            int sp = shortestMinePaths.stream()
                .map(ssp -> {
                  GraphPath<Site, River> gp = ssp.getPath(targetVertex);
                  if (gp == null) {
                    return Integer.MAX_VALUE;
                  } else {
                    return gp.getLength();
                  }
                })
                .min(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE);
            targetVertex.setShortestPathToAnyMine(sp);
            if (sp < minShortestPath) {
              minShortestPath = sp;
            }
          }
        }

        // Apply the weights
        for (River river : graph.edgesOf(site)) {
          if (!river.isClaimed()) {
            Site targetVertex = state.getSiteToMap().get(river.getSource()) == site ?
                state.getSiteToMap().get(river.getTarget()) : state.getSiteToMap().get(river.getSource());

            if (targetVertex.getShortestPathToAnyMine() == minShortestPath) {
              setter.apply(river, weight);
            }
          }
        }
      }
    });
  }
}
