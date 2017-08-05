package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.State;

import java.util.function.DoubleUnaryOperator;

abstract public class BaseAlgorithm implements GraphAlgorithm {
  protected final Getter getter;
  protected final Setter setter;

  public BaseAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    this.getter = getter;
    this.setter = setter;
  }

  final void alter(final River river, final DoubleUnaryOperator f) {
    setter.apply(river, f.applyAsDouble(getter.apply(river)));
  }

  public abstract void iterate(State state);

  public void run(Algorithms algorithm, final State state) {
    Long timer = System.currentTimeMillis();
    iterate(state);
    state.setLastTime(algorithm, System.currentTimeMillis() - timer);
  }
}
