package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.collect.ImmutableSet;
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
    for (Site mine: state.getMines()) {
      boolean connected = false;
      for (River river : graph.edgesOf(mine)) {
        if (river.getClaimedBy() == state.getPunter()) {
          connected = true;
          break;
        }
      }
      Double mineWeight = Weights.Max;
      if (connected) {
        mineWeight = Weights.HighlyDesired;
      }
      for (River river : graph.edgesOf(mine)) {
        setter.apply(river, mineWeight);
      }
    }
  }
}
