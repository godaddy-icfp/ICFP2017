package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.State;

abstract public class BaseAlgorithm implements GraphAlgorithm {

  public abstract void iterate(State state);

  public void run(final State state) {
    iterate(state);
  }
}
