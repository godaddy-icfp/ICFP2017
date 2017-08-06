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

  private int ownClaimCount;

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

  public int getOwnClaimCount() {
    return ownClaimCount;
  }

  public void setOwnClaimCount(int ownClaimCount) {
    this.ownClaimCount = ownClaimCount;
  }
}
