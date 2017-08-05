package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.State;

abstract public class BaseAlgorithm implements GraphAlgorithm {

  public abstract void iterate(State state);

  public void run(Algorithms algorithm, final State state) {
    Long timer = System.currentTimeMillis();
    iterate(state);
    state.setLastTime(algorithm, System.currentTimeMillis() - timer);
  }
}
