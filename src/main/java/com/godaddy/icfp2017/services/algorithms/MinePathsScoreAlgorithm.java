package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Pair;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class MinePathsScoreAlgorithm extends BaseAlgorithm implements GraphAlgorithm {
  public MinePathsScoreAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {

    final SimpleWeightedGraph<Site, River> graph = state.getGraph();


    final DijkstraShortestPath<Site, River> shortestPath = new DijkstraShortestPath<>(graph);
    final ImmutableSet<ShortestPathAlgorithm.SingleSourcePaths<Site, River>> shortestMinePaths = state
        .getMines()
        .stream()
        .map(shortestPath::getPaths)
        .collect(ImmutableSet.toImmutableSet());

    state.getMines()
         .forEach(mine -> {
           final HashSet<Integer> visted = Sets.newHashSet();

           final Integer[] siteToMineDistance = new Integer[graph.vertexSet().size()];
           for (int i = 0; i < siteToMineDistance.length; i++) {
             siteToMineDistance[i] = Integer.MAX_VALUE;
           }

           final ShortestPathAlgorithm.SingleSourcePaths<Site, River> pathSources = shortestPath.getPaths(mine);

           final int maxDistance = 7;

           PriorityQueue<Pair<Integer, Site>> queue = new PriorityQueue<>(Comparator.comparing(p -> p.left));

           queue.add(Pair.of(0, mine));
           visted.add(mine.getId());

           while (!queue.isEmpty()) {
             final Pair<Integer, Site> remove = queue.remove();

             final int newDistance = remove.left + 1;
             final Site site = remove.right;

             final Set<River> rivers = graph.edgesOf(site);
             for (final River river : rivers) {

               final Site sourceSite = state.getSiteToMap().get(river.getSource());
               final Site targetSite = state.getSiteToMap().get(river.getTarget());

               if (site.getId() != sourceSite.getId()) {
                 computeDistance(pathSources, siteToMineDistance, sourceSite);
                 if (!visted.contains(sourceSite.getId()) && newDistance < maxDistance) {
                   queue.add(Pair.of(newDistance, sourceSite));
                 }
               }
               else {
                 computeDistance(pathSources, siteToMineDistance, targetSite);
                 if (!visted.contains(targetSite.getId()) && newDistance < maxDistance) {
                   queue.add(Pair.of(newDistance, targetSite));
                 }
               }

               final int min =
                   Math.min(siteToMineDistance[targetSite.getId()] * siteToMineDistance[targetSite.getId()],
                            siteToMineDistance[sourceSite.getId()] * siteToMineDistance[sourceSite.getId()]);

               setter.apply(river, sigmoid(min));
             }
           }
         });
  }

  public static double sigmoid(double x) {
    return (1 / (1 + Math.pow(Math.E, (-1 * x))));
  }

  private void computeDistance(
      final ShortestPathAlgorithm.SingleSourcePaths<Site, River> pathSource,
      final Integer[] siteToMineDistance,
      final Site targetSite) {
    final GraphPath<Site, River> targetPath = pathSource.getPath(targetSite);
    final int targetLength = targetPath.getLength();
    siteToMineDistance[targetSite.getId()] = Math.min(targetLength, siteToMineDistance[targetSite.getId()]);
  }
}
