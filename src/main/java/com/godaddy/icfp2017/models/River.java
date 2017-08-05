package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class River {
  @JsonProperty("source")
  private int source;

  @JsonProperty("target")
  private int target;

  @JsonProperty("algorithmWeights")
  private HashMap<String, Double> algorithmWeights = new HashMap<String, Double>();

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

  public HashMap<String, Double> getAlgorithmWeights() { return algorithmWeights; }

  public void setAlgorithmWeights(final HashMap<String, Double> algorithmWeights) { this.algorithmWeights = algorithmWeights; }
}
