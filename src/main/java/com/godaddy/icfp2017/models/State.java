package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.godaddy.icfp2017.models.serialization.BinaryStateDeserializer;
import com.godaddy.icfp2017.models.serialization.BinaryStateSerializer;
import com.godaddy.icfp2017.services.analysis.Timeable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

@JsonSerialize(using = BinaryStateSerializer.class)
@JsonDeserialize(using = BinaryStateDeserializer.class)
public class State {
  @JsonProperty
  private int punter;

  @JsonProperty
  private SimpleWeightedGraph<Site, River> graph;

  @JsonProperty
  private SimpleWeightedGraph<Site, River> graphOfEnemyMoves;

  private ImmutableMap<Integer, Site> siteToMap;

  public Long getLastTime(final Timeable algorithm) {
    return lastTimes.get(algorithm.timeKey());
  }

  public ConcurrentHashMap<String, Long> getLastTimes() {
    return lastTimes;
  }

  public State() {
    lastTimes = new ConcurrentHashMap<>();
  }

  public void setLastTime(Timeable timeable, Long lastTime) {
    this.lastTimes.put(timeable.timeKey(), lastTime);
  }

  @JsonProperty
  private transient ConcurrentHashMap<String, Long> lastTimes;

  @JsonProperty
  private int punters;

  @JsonProperty
  private ImmutableSet<Site> mines;

  private transient FloydWarshallShortestPaths<Site, River> shortestPaths;

  private SortedSet<Path> rankedPaths;

  private ImmutableList<GraphPath<Site, River>> mineToMinePaths;

  public int getPunter() {
    return punter;
  }

  public void setPunter(final int punter) {
    this.punter = punter;
  }

  public SimpleWeightedGraph<Site, River> getGraph() {
    return graph;
  }

  public void setGraph(final SimpleWeightedGraph<Site, River> graph) {
    this.graph = graph;
  }

  public SimpleWeightedGraph<Site, River> getGraphOfEnemyMoves() {
    return graphOfEnemyMoves;
  }

  public void setGraphOfEnemyMoves(final SimpleWeightedGraph<Site, River> graph) {
    this.graphOfEnemyMoves = graph;
  }


  public void setMines(final ImmutableSet<Site> mines) {
    this.mines = mines;
  }

  public ImmutableSet<Site> getMines() {
    return mines;
  }

  public void setShortestPaths(final FloydWarshallShortestPaths<Site, River> shortestPaths) {
    this.shortestPaths = shortestPaths;
  }

  public FloydWarshallShortestPaths<Site, River> getShortestPaths() {
    return shortestPaths;
  }

  public SortedSet<Path> getRankedPaths() {
    return rankedPaths;
  }

  public void setRankedPaths(final SortedSet<Path> rankedPaths) {
    this.rankedPaths = rankedPaths;
  }

  public int getPunters() {
    return punters;
  }

  public void setPunters(int punters) {
    this.punters = punters;
  }

  public ImmutableList<GraphPath<Site, River>> getMineToMinePaths() {
    return mineToMinePaths;
  }

  public void setMineToMinePaths(final ImmutableList<GraphPath<Site, River>> paths) {
    this.mineToMinePaths = paths;
  }

  public void setSiteToMap(final ImmutableMap<Integer, Site> siteToMap) {
    this.siteToMap = siteToMap;
  }

  public ImmutableMap<Integer, Site> getSiteToMap() {
    return siteToMap;
  }
}
