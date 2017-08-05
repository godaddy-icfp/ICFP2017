package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jgrapht.graph.SimpleWeightedGraph;

public class State {
  @JsonProperty
  private int punter;

  @JsonProperty
  private SimpleWeightedGraph<Site, River> map;

  @JsonProperty
  private int moveCount;

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
}
