package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class River {
  @JsonProperty("source")
  private int source;

  @JsonProperty("target")
  private int target;

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
}
