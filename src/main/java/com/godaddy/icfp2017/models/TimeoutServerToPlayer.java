package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonValue;

public class TimeoutServerToPlayer implements ServerToPlayer {
  @JsonValue
  private double timeout;

  public double getTimeout() {
    return timeout;
  }

  public void setTimeout(final double timeout) {
    this.timeout = timeout;
  }
}
