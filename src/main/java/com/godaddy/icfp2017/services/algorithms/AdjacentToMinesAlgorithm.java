package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;

final public class AdjacentToMinesAlgorithm extends BaseAlgorithm {

  public AdjacentToMinesAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
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
