package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.services.analysis.Timeable;

public enum Algorithms implements Timeable {
  AdjacentToMine,
  AdjacentToPath,
  ConnectedDecision,
  Connectedness,
  MineToMine,
  MinimumSpanningTree,
  EnemyPath,
  ScoringAlgo;


  @Override
  public String timeKey() {
    return "Algorithms: " + name();
  }
}
