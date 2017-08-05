package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

class RiverWeightingAlgorithm {
  static FloydWarshallShortestPaths<Site, River> apply(
      final SimpleWeightedGraph<Site, River> map,
      final ImmutableSet<Site> mines) {
    final FloydWarshallShortestPaths<Site, River> shortestPaths = new FloydWarshallShortestPaths<>(map);
    final BreadthFirstIterator<Site, River> iter = new BreadthFirstIterator<>(map);
    while (iter.hasNext()) {
      final Site site = iter.next();
      for (final Site mine : mines) {
        final double weight = shortestPaths.getPathWeight(site, mine);
        System.out.println(String.format("%s->%f->%s", site, weight, mine));
      }
    }

    return shortestPaths;
  }
}
