package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.HashMap;
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
      double totalEdges = 0.0;
      double takenEdges = 0.0;

      // We take the minimum from each site (just like the weighing algorithm)
      HashMap<Site, Integer> distances = new HashMap<>();

      // Merge from graph and enemy graph
      ImmutableSet<River> mergedRivers = ImmutableSet.<River>builder().addAll(graph.edgesOf(site)).addAll(graphOfEnemyMoves.edgesOf(site)).build();

      for (River river : mergedRivers) {
        if (river.isClaimed()) takenEdges += 1;
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

      // Compute the max of the distances
      double maxDistance = distances.values().stream().max(Comparator.naturalOrder()).orElse(0);
      maxDistance *= maxDistance;

      // Weight computation
      double weight = 0.5 + (
          0.5 * (maxDistance / siteExp) * (takenEdges/totalEdges)
          );

      // Set the weight on all outgoing edges

      for (River river : graph.edgesOf(site)) {
        if (!river.isClaimed()) {
          setter.apply(river, weight);
        }
      }
    });
  }
}
