package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;

@FunctionalInterface
public interface AlgorithmFactory {
  GraphAlgorithm create(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter);
}
