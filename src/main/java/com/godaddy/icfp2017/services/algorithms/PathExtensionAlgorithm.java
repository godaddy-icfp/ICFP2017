package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Pair;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PathExtensionAlgorithm extends BaseAlgorithm {
  private static final int beamWidth = 5;

  public PathExtensionAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    final ImmutableSet<Site> mines = state.getMines();
    final DijkstraShortestPath<Site, River> shortestPath = new DijkstraShortestPath<>(graph);
    final ImmutableSet<ShortestPathAlgorithm.SingleSourcePaths<Site, River>> shortestMinePaths = mines
        .stream()
        .map(shortestPath::getPaths)
        .collect(ImmutableSet.toImmutableSet());

    final Comparator<Pair<River, Site>> comparator = (o1, o2) -> {
      final int i1 = shortestMinePaths
          .stream()
          .mapToInt(site -> site.getPath(o1.right).getLength())
          .max()
          .orElse(0);
      final int i2 = shortestMinePaths
          .stream()
          .mapToInt(site -> site.getPath(o2.right).getLength())
          .max()
          .orElse(0);
      return -Integer.compare(i1, i2);
    };

    final PriorityQueue<Pair<River, Site>> queue = new PriorityQueue<>(comparator);

    queue.addAll(mines
        .stream()
        .flatMap(mine -> graph
            .edgesOf(mine)
            .stream()
            .flatMap(river -> Stream.of(
                Pair.of(river, graph.getEdgeSource(river)),
                Pair.of(river, graph.getEdgeTarget(river)))))
        .filter(site -> !mines.contains(site))
        .collect(Collectors.toSet()));

    final ArrayList<Pair<River, Site>> topK = new ArrayList<>();
    final HashSet<Site> seen = new HashSet<>();
    while (!queue.isEmpty()) {
      final Pair<River, Site> site = queue.poll();
      if (seen.contains(site.right)) {
        continue;
      }

      graph
          .edgesOf(site.right)
          .stream()
          .flatMap(river -> Stream.of(
            Pair.of(river, graph.getEdgeSource(river)),
            Pair.of(river, graph.getEdgeTarget(river))))
          .filter(value -> !seen.contains(value.right))
          .forEach(value -> {
            if (topK.size() < beamWidth) {
              topK.add(value);
            } else if (comparator.compare(value, topK.get(0)) < 0) {
              queue.add(value);
              final Pair<River, Site> previous = topK.get(0);
              final River river = previous.left.isClaimed() ? value.left : previous.left;
              topK.set(0, Pair.of(river, value.right));
              topK.sort(comparator);
            }
          });

      seen.add(site.right);
    }

    setter.apply(topK.get(topK.size() - 1).left, Weights.Max);
  }
}
