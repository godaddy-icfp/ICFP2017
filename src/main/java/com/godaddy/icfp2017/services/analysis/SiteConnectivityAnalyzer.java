package com.godaddy.icfp2017.services.analysis;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class SiteConnectivityAnalyzer extends BaseAnalyzer {

  public SiteConnectivityAnalyzer() {
    super();
  }

  @Override
  public void analyze(State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    graph.vertexSet().forEach(site -> {
      site.setTotalConnectedRivers(graph.edgesOf(site).size());

      int ownedConnectedRiversCount = 0;
      for (River river: graph.edgesOf(site)) {
        if (river.getClaimedBy() == state.getPunter()) {
          ownedConnectedRiversCount++;
        }
      }
      site.setOwnedConnectedRivers(ownedConnectedRiversCount);
    });
  }
}
