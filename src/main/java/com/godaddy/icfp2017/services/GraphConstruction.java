package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.graph.SimpleWeightedGraph;

final class GraphConstruction {
  final ImmutableSet<Site> mines;
  final SimpleWeightedGraph<Site, River> graph;
  final SimpleWeightedGraph<Site, River> graphOfEnemyMoves;
  final ImmutableMap<Integer, Site> siteToMap;

  GraphConstruction(
      final ImmutableSet<Site> mines,
      final SimpleWeightedGraph<Site, River> graph,
      final SimpleWeightedGraph<Site, River> graphOfEnemyMoves,
      final ImmutableMap<Integer, Site> siteToMap) {
    this.mines = mines;
    this.graph = graph;
    this.graphOfEnemyMoves = graphOfEnemyMoves;
    this.siteToMap = siteToMap;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final GraphConstruction that = (GraphConstruction) o;
    return Objects.equal(mines, that.mines) &&
        Objects.equal(graph, that.graph) &&
        Objects.equal(graphOfEnemyMoves, that.graphOfEnemyMoves) &&
        Objects.equal(siteToMap, that.siteToMap);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mines, graph, graphOfEnemyMoves, siteToMap);
  }
}
