package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Site {
  @JsonProperty("id")
  private int id;

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }
}
