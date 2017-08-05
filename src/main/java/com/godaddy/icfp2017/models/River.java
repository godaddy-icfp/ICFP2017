package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.icfp2017.services.Algorithms;
import com.google.common.base.Objects;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;

public class River extends DefaultWeightedEdge {
  @JsonProperty("source")
  private int source;

  @JsonProperty("target")
  private int target;

  @JsonIgnore
  @JsonProperty("algorithmWeights")
  private HashMap<Algorithms, Double> algorithmWeights = new HashMap<>();

  @JsonProperty("claimedBy")
  private int claimedBy = -1;

  public River() {
  }

  public River(final int source, final int target) {
    this.source = source;
    this.target = target;
  }

  public Integer getSource() {
    return source;
  }

  public void setSource(final int source) {
    this.source = source;
  }

  public Integer getTarget() {
    return target;
  }

  public void setTarget(final int target) {
    this.target = target;
  }

  public HashMap<Algorithms, Double> getAlgorithmWeights() {
    return algorithmWeights;
  }

  public void setAlgorithmWeights(final HashMap<Algorithms, Double> algorithmWeights) {
    this.algorithmWeights = algorithmWeights;
  }

  public int getClaimedBy() {
    return claimedBy;
  }

  public void setClaimedBy(int claimedBy) {
    this.claimedBy = claimedBy;
  }

  public boolean isClaimed() {
    return this.claimedBy >= 0;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof River &&
           ((River) obj).source == source &&
           ((River) obj).target == target;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source, target);
  }
}
