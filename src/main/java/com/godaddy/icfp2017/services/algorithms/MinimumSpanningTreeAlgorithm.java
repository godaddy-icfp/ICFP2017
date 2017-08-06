package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.alg.spanning.BoruvkaMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

import java.util.stream.Stream;

public class MinimumSpanningTreeAlgorithm extends BaseAlgorithm {

  public MinimumSpanningTreeAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    final ImmutableSet<Site> vertices = state
        .getMineToMinePaths()
        .stream()
        .flatMap(paths -> paths.getVertexList().stream())
        .flatMap(x -> graph
            .edgesOf(x)
            .stream()
            .flatMap(river -> Stream.of(
                graph.getEdgeSource(river),
                graph.getEdgeTarget(river))))
        .collect(ImmutableSet.toImmutableSet());

    final UndirectedWeightedSubgraph<Site, River> subgraph =
        new UndirectedWeightedSubgraph<>(state.getGraph(), vertices);

    final BoruvkaMinimumSpanningTree<Site, River> minimumSpanningTree =
        new BoruvkaMinimumSpanningTree<>(subgraph);

    minimumSpanningTree.getSpanningTree().getWeight();

    for (final River river : minimumSpanningTree.getSpanningTree().getEdges()) {
//      System.out.println(river);
      alter(river, value -> Math.min(Weights.Desired, value * 1.1));
    }
  }
}
