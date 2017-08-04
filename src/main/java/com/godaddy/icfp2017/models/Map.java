package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Map {
  @JsonProperty("sites")
  private List<Site> sites;

  @JsonProperty("rivers")
  private List<River> rivers;

  @JsonProperty("mines")
  private List<Integer> mines;

  public List<Site> getSites() {
    return sites;
  }

  public void setSites(final List<Site> sites) {
    this.sites = sites;
  }

  public List<River> getRivers() {
    return rivers;
  }

  public void setRivers(final List<River> rivers) {
    this.rivers = rivers;
  }

  public List<Integer> getMines() {
    return mines;
  }

  public void setMines(final List<Integer> mines) {
    this.mines = mines;
  }
}
