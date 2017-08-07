package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Site implements Serializable {
  @JsonProperty("id")
  private int id;
  private boolean isMine;

  private double x;
  private double y;

  private static final long serialVersionUID = 2513236575731581196L;

  // Statistics based on radius zero (edges connected directly)
  private int ownedConnectedRivers;
  private int totalConnectedRivers;
  private boolean isOwned;
  private int shortestPathToAnyMine = Integer.MAX_VALUE;

  // Statistics based on the connected edges for all nodes in radius around site
  private int radiusMark;
  private int radiusConnectedRadius;
  private int radiusOwnedConnectedRivers;
  private int radiusTotalConnectedRivers;

  public Site() {
  }

  public Site(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Site && ((Site) obj).id == id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return Integer.toString(id);
  }

  public boolean isMine() {
    return isMine;
  }

  public void setMine(final boolean mine) {
    isMine = mine;
  }

  public double getY() {
    return y;
  }

  public void setY(final double y) {
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public void setX(final double x) {
    this.x = x;
  }

  public int getOwnedConnectedRivers() {
    return ownedConnectedRivers;
  }

  public void setOwnedConnectedRivers(int ownedConnectedRivers) {
    this.ownedConnectedRivers = ownedConnectedRivers;
  }

  public int getTotalConnectedRivers() { return totalConnectedRivers; }

  public void setTotalConnectedRivers(int totalConnectedRivers) { this.totalConnectedRivers = totalConnectedRivers; }

  public boolean isOwned() { return ownedConnectedRivers > 0; }

  public int getRadiusConnectedRadius() {
    return radiusConnectedRadius;
  }

  public void setRadiusConnectedRadius(int radiusConnectedRadius) {
    this.radiusConnectedRadius = radiusConnectedRadius;
  }

  public int getRadiusOwnedConnectedRivers() {
    return radiusOwnedConnectedRivers;
  }

  public void setRadiusOwnedConnectedRivers(int radiusOwnedConnectedRivers) {
    this.radiusOwnedConnectedRivers = radiusOwnedConnectedRivers;
  }

  public int getRadiusTotalConnectedRivers() {
    return radiusTotalConnectedRivers;
  }

  public void setRadiusTotalConnectedRivers(int radiusTotalConnectedRivers) {
    this.radiusTotalConnectedRivers = radiusTotalConnectedRivers;
  }

  public int getRadiusMark() {
    return radiusMark;
  }

  public void setRadiusMark(int radiusMark) {
    this.radiusMark = radiusMark;
  }

  public int getShortestPathToAnyMine() { return shortestPathToAnyMine; }
  public void setShortestPathToAnyMine(int s) { shortestPathToAnyMine = s; }
}
