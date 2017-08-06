package com.godaddy.icfp2017.models;

public class TimeoutServerToPlayer implements ServerToPlayer {
  public double getT() {
    return t;
  }

  public void setT(final double t) {
    this.t = t;
  }

  private double t;
}
