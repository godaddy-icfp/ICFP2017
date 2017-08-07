package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.BoruvkaMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

import java.util.Objects;

public class MinimumSpanningTreeAlgorithm extends BaseAlgorithm {

  public MinimumSpanningTreeAlgorithm(final Getter getter, final Setter setter) {
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

    final ImmutableList<GraphPath<Site, River>> selectedPaths = state
        .getMineToMinePaths()
        .stream()
        .flatMap(paths -> paths.getVertexList().stream())
        .flatMap(site -> shortestMinePaths
            .stream()
            .map(shortestMinePath -> shortestMinePath.getPath(site)))
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());

    final ImmutableSet<Site> vertices = selectedPaths
        .stream()
        .flatMap(path -> path.getVertexList().stream())
        .collect(ImmutableSet.toImmutableSet());

    final ImmutableSet<River> edges = selectedPaths
        .stream()
        .flatMap(path -> path.getEdgeList().stream())
        .collect(ImmutableSet.toImmutableSet());

    final UndirectedWeightedSubgraph<Site, River> subgraph =
        new UndirectedWeightedSubgraph<>(state.getGraph(), vertices, edges);

    final BoruvkaMinimumSpanningTree<Site, River> minimumSpanningTree =
        new BoruvkaMinimumSpanningTree<>(subgraph);

    for (final River river : minimumSpanningTree.getSpanningTree().getEdges()) {
      setter.apply(river, Weights.Desired);
    }
  }
}
