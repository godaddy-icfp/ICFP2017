package com.godaddy.icfp2017.services.analysis;

public enum Analyzers implements Timeable {
  SiteConnectivity,
  MineToMinePath;

  @Override
  public String timeKey() {
    return "Analyzers: " + name();
  }
}
