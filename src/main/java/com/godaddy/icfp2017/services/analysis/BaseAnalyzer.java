package com.godaddy.icfp2017.services.analysis;

import com.godaddy.icfp2017.models.State;

public abstract class BaseAnalyzer implements GraphAnalyzer {

  public abstract void analyze(State state);

  public void run(Analyzers analyzer, final State state) {
    Long timer = System.currentTimeMillis();
    state.setLastTime(analyzer, 1000000L); // Set a sentinel value
    analyze(state);
    state.setLastTime(analyzer, System.currentTimeMillis() - timer);
  }
}
