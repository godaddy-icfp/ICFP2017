package com.godaddy.icfp2017.services.analysis;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class StrategyAnalyzer extends BaseAnalyzer {

  private final int RADIUS_CONNECTION_CHECK_DEPTH = 2;

  private class StatisticsAggregator {
    final int traversalMark;
    int ownedCount;
    int totalCount;

    public StatisticsAggregator(int traversalMark) {
      this.traversalMark = traversalMark;
    }
  }

  @Override
  public void analyze(State state) {
    int totalClaimed = 0;
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    for (Site site: graph.vertexSet()) {
      if (site.isOwned()) {
        totalClaimed++;
      }
      if (site.isMine()) {
        StatisticsAggregator aggregator = new StatisticsAggregator(site.getId());
        this.visitRadius(state, site, aggregator, RADIUS_CONNECTION_CHECK_DEPTH);
        site.setRadiusOwnedConnectedRivers(aggregator.ownedCount);
        site.setRadiusTotalConnectedRivers((aggregator.totalCount));
        site.setRadiusConnectedRadius(RADIUS_CONNECTION_CHECK_DEPTH);
      } else {
        site.setRadiusConnectedRadius(0);
        site.setRadiusOwnedConnectedRivers(site.getOwnedConnectedRivers());
        site.setRadiusTotalConnectedRivers(site.getTotalConnectedRivers());
      }
    }
    state.setTotalOwned(totalClaimed);
  }

  private void visitRadius(State state, Site site, StatisticsAggregator aggregator, final int depth) {
    if (depth == 0 || site.getRadiusMark() == aggregator.traversalMark) {
      return;
    }
    site.setRadiusMark(aggregator.traversalMark);
    aggregator.ownedCount += site.getOwnedConnectedRivers();
    aggregator.totalCount += site.getTotalConnectedRivers();
    state.getGraph().edgesOf(site).forEach(river -> {
      this.visitRadius(state, state.getGraph().getEdgeSource(river), aggregator, depth-1);
      this.visitRadius(state, state.getGraph().getEdgeTarget(river), aggregator, depth-1);
    });
  }
}
