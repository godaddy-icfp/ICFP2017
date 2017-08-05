package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandshakeS2P implements ServerToPlayer {
  @JsonProperty("you")
  private String you;

  public String getYou() {
    return you;
  }

  public void setYou(final String you) {
    this.you = you;
  }
}
