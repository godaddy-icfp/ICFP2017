package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.alg.spanning.BoruvkaMinimumSpanningTree;

public class MinimumSpanningTreeAlgorithm extends BaseAlgorithm {

  private final Getter getter;
  private final Setter setter;

  public MinimumSpanningTreeAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    this.getter = getter;
    this.setter = setter;
  }

  @Override
  public void iterate(final State state) {
    final BoruvkaMinimumSpanningTree<Site, River> minimumSpanningTree =
        new BoruvkaMinimumSpanningTree<>(state.getGraph());

    for (final River river : minimumSpanningTree.getSpanningTree().getEdges()) {
      setter.apply(river, getter.apply(river) * 1.1);
    }
  }
}
