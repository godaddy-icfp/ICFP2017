package com.godaddy.icfp2017.services.analysis;

import com.godaddy.icfp2017.models.State;

public interface GraphAnalyzer {
  void run(String algorithm, final State state);
}
