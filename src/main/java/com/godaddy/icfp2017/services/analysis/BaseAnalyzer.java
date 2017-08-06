package com.godaddy.icfp2017.services.analysis;

import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.Algorithms;

public abstract class BaseAnalyzer implements GraphAnalyzer {

  public abstract void analyze(State state);

  public void run(String algorithm, final State state) {
    Long timer = System.currentTimeMillis();
    state.setLastTime(algorithm, 1000000L); // Set a sentinel value
    analyze(state);
    state.setLastTime(algorithm, System.currentTimeMillis() - timer);
  }
}
