package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandShakeP2S implements P2S {
  @JsonProperty("me")
  private String me;

  public String getMe() {
    return me;
  }

  public void setMe(final String me) {
    this.me = me;
  }
}
