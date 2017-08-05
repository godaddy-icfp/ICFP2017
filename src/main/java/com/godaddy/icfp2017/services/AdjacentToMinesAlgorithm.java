package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToMinesAlgorithm {
  private SimpleWeightedGraph<Site, River> graph;

  public AdjacentToMinesAlgorithm(SimpleWeightedGraph<Site, River> graph) {
    this.graph = graph;
  }

  public void run() {
    this.graph.vertexSet()
              .forEach(site -> {
                this.graph.edgesOf(site)
                          .forEach(river -> {
                            if (site.isMine()) {
                              river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Max);
                            }
                            else {
                              river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Identity);
                            }
                          });
              });
  }
}
