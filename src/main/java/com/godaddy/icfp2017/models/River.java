package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.icfp2017.services.Algorithms;

import java.util.HashMap;

public class River {
  @JsonProperty("source")
  private int source;

  @JsonProperty("target")
  private int target;

  @JsonIgnore
  @JsonProperty("algorithmWeights")
  private HashMap<Algorithms, Double> algorithmWeights = new HashMap<>();

  @JsonProperty("claimedBy")
  private int claimedBy = -1;

  public int getSource() {
    return source;
  }

  public void setSource(final int source) {
    this.source = source;
  }

  public int getTarget() {
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
}
