package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.analysis.Analyzers;
import com.godaddy.icfp2017.services.analysis.GraphAnalyzer;
import com.godaddy.icfp2017.services.analysis.MineToMinePathAnalyzer;
import com.godaddy.icfp2017.services.analysis.SiteConnectivityAnalyzer;
import com.godaddy.icfp2017.services.analysis.StrategyAnalyzer;
import com.google.common.collect.ImmutableMap;

public class GameAnalyzer {

  private final ImmutableMap<Analyzers, GraphAnalyzer> analyzers =
      ImmutableMap.of(
          Analyzers.SiteConnectivity, new SiteConnectivityAnalyzer(),
          Analyzers.MineToMinePath, new MineToMinePathAnalyzer(),
          Analyzers.Strategy, new StrategyAnalyzer() // Must run after SiteConnectivity
      );

  public void run(State state) {
    analyzers.keySet().forEach(key -> analyzers.get(key).run(key, state));
  }
}
