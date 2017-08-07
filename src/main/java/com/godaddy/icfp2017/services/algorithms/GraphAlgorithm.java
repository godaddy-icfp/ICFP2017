package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.State;

public interface GraphAlgorithm {
  @FunctionalInterface
  interface Getter {
    double apply(River river);
  }

  @FunctionalInterface
  interface Setter {
    double apply(River river, double score);
  }

  void run(final Algorithms algorithm, final State state);
}
