package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

class RiverWeightingAlgorithm {
  static FloydWarshallShortestPaths<Site, River> apply(
      final SimpleWeightedGraph<Site, River> map,
      final ImmutableSet<Site> mines) {
    return new FloydWarshallShortestPaths<>(map);
  }
}
