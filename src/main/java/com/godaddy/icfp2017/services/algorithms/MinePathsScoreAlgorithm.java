package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

public class MinePathsScoreAlgorithm extends BaseAlgorithm implements GraphAlgorithm {
  public MinePathsScoreAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {

    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    final Set<Site> sites = graph.vertexSet();
    final ImmutableMap<Integer, Site> siteLookup = Maps.uniqueIndex(sites, s -> s.getId());

    sites.stream()
         .filter(s -> s.isMine())
         .forEach(mine -> {
           final Integer[] siteToMineDistance = new Integer[graph.vertexSet().size()];
           for (int i = 0; i < siteToMineDistance.length; i++) {
             siteToMineDistance[i] = Integer.MAX_VALUE;
           }

           final int maxDistance = 10;

           PriorityQueue<Pair<Integer, Site>> queue = new PriorityQueue<>(Comparator.comparing(p -> p.left));

           queue.add(Pair.of(0, mine));

           while (!queue.isEmpty()) {
             final Pair<Integer, Site> remove = queue.remove();

             if (remove.left > maxDistance) {
               break;
             }

             final int newDistance = remove.left + 1;
             final Site site = remove.right;

             final Set<River> rivers = graph.edgesOf(site);
             for (final River river : rivers) {

               final Site sourceSite = siteLookup.get(river.getSource());
               final Site targetSite = siteLookup.get(river.getTarget());

               if (site.getId() != sourceSite.getId()) {
                 computeDistance(graph, mine, siteToMineDistance, sourceSite);
                 queue.add(Pair.of(newDistance, sourceSite));
               }
               else {
                 computeDistance(graph, mine, siteToMineDistance, targetSite);
                 queue.add(Pair.of(newDistance, targetSite));
               }

               setter.apply(river, sigmoid(Math.min(siteToMineDistance[targetSite.getId()],
                                                    siteToMineDistance[sourceSite.getId()])));
             }
           }
         });
  }

  public static double sigmoid(double x) {
    return (1 / (1 + Math.pow(Math.E, (-1 * x))));
  }

  private void computeDistance(
      final SimpleWeightedGraph<Site, River> graph,
      final Site mine,
      final Integer[] siteToMineDistance, final Site targetSite) {
    final GraphPath<Site, River> targetPath = DijkstraShortestPath.findPathBetween(graph,
                                                                                   targetSite,
                                                                                   mine);
    final int targetLength = targetPath.getLength();
    siteToMineDistance[targetSite.getId()] = Math.min(targetLength,
                                                      siteToMineDistance[targetSite.getId()]);
  }
}
