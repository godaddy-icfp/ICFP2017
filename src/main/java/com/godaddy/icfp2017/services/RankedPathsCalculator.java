package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.Path;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.SortedSet;
import java.util.TreeSet;

public class RankedPathsCalculator {
  private State state;
  private SimpleWeightedGraph<Site, River> graph;

  public RankedPathsCalculator(final State state) {
    this.state = state;
    this.graph = state.getGraph();
  }

  public SortedSet<Path> calculate() {
    SortedSet<Path> rankedPaths = new TreeSet<Path>();

    state.getMineToMinePaths()
        .forEach(minePath -> {
          int length = minePath.getLength();
          int score = length * length;
          score *=2;

          int sourceMine = minePath.getStartVertex().getId();
          int targetMine = minePath.getEndVertex().getId();
          Path rankedPath = new Path(sourceMine, targetMine, score, length);
          rankedPaths.add(rankedPath);
        });


    /*this.state.getMines().forEach(mine -> {
        this.graph.vertexSet().forEach(site -> {
          if (mine.getId() != site.getId()) {
            GraphPath<Site, River> shortestPath = this.state.getShortestPaths().getPath(mine, site);

            int score = shortestPath.getLength();
            score *= score;
            if (site.isMine())
              score *=2;

            Path rankedPath = new Path(mine.getId(), site.getId(), score);
            rankedPaths.add(rankedPath);
          }
        });
      });*/

    return rankedPaths;
  }
}
