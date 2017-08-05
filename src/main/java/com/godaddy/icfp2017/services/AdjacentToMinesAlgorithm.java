package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToMinesAlgorithm implements GraphAlgorithm {
  private final Algorithms algorithm;
  private SimpleWeightedGraph<Site, River> graph;

  public AdjacentToMinesAlgorithm(final Algorithms algorithm, final State state) {
    this.algorithm = algorithm;
    this.graph = state.getGraph();
  }

  @Override
  public void run() {
    this.graph.edgeSet()
              .forEach(river -> {
                if (graph.getEdgeSource(river).isMine() || graph.getEdgeTarget(river).isMine()) {
                  river.getAlgorithmWeights().put(algorithm, Weights.Max);
                }
                else {
                  river.getAlgorithmWeights().put(algorithm, Weights.Identity);
                }
              });
  }
}
