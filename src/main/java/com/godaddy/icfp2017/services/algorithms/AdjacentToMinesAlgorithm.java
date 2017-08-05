package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;

final public class AdjacentToMinesAlgorithm implements GraphAlgorithm {
  private final Setter setter;

  public AdjacentToMinesAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    this.setter = setter;
  }

  @Override
  public void run(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    graph.edgeSet()
        .forEach(river -> {
          if (graph.getEdgeSource(river).isMine() || graph.getEdgeTarget(river).isMine()) {
            setter.apply(river, Weights.Max);
          }
          else {
            setter.apply(river, Weights.Identity);
          }
        });
  }
}
