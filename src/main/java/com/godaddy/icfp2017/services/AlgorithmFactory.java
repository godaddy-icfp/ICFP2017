package com.godaddy.icfp2017.services;

@FunctionalInterface
public interface AlgorithmFactory {
  GraphAlgorithm create(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter);
}
