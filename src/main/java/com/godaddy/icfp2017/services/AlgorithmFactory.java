package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.State;

@FunctionalInterface
public interface AlgorithmFactory {
  GraphAlgorithm create(final State state);
}
