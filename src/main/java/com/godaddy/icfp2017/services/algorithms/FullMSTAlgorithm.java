package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import org.jgrapht.alg.spanning.BoruvkaMinimumSpanningTree;

public class FullMSTAlgorithm extends BaseAlgorithm {
  public FullMSTAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final BoruvkaMinimumSpanningTree<Site, River> minimumSpanningTree =
        new BoruvkaMinimumSpanningTree<>(state.getGraph());
    minimumSpanningTree.getSpanningTree()
                       .getEdges()
                       .stream()
                       .filter(r -> !r.isClaimed())
                       .forEach(r -> setter.apply(r, Weights.Max));
  }
}
