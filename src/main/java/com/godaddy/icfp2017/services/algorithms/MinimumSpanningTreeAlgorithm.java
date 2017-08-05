package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.alg.spanning.BoruvkaMinimumSpanningTree;

public class MinimumSpanningTreeAlgorithm extends BaseAlgorithm {

  public MinimumSpanningTreeAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final BoruvkaMinimumSpanningTree<Site, River> minimumSpanningTree =
        new BoruvkaMinimumSpanningTree<>(state.getGraph());

    for (final River river : minimumSpanningTree.getSpanningTree().getEdges()) {
      alter(river, value -> value * 1.1);
    }
  }
}
