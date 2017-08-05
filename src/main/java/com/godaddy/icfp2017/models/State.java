package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

public class State {
  @JsonProperty
  private int punter;

  @JsonProperty
  private SimpleWeightedGraph<Site, River> map;

  @JsonProperty
  private int moveCount;

  @JsonProperty
  private ImmutableSet<Site> mines;

  private FloydWarshallShortestPaths<Site, River> shortestPaths;

  public int getPunter() {
    return punter;
  }

  public void setPunter(final int punter) {
    this.punter = punter;
  }

  public SimpleWeightedGraph<Site, River> getMap() {
    return map;
  }

  public void setMap(final SimpleWeightedGraph<Site, River> map) {
    System.err.println(map.toString());
    this.map = map;
  }

  public int getMoveCount() { return moveCount; }

  public void setMoveCount(final int moveCount) {
    this.moveCount = moveCount;
  }

  public void setMines(final ImmutableSet<Site> mines) {
    this.mines = mines;
  }

  public ImmutableSet<Site> getMines() {
    return mines;
  }

  public void setShortestPaths(final FloydWarshallShortestPaths<Site,River> shortestPaths) {
    this.shortestPaths = shortestPaths;
  }

  public FloydWarshallShortestPaths<Site, River> getShortestPaths() {
    return shortestPaths;
  }
}
