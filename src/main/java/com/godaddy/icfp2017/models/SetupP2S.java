package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetupP2S implements PlayerToServer {
  @JsonProperty("ready")
  private int ready;

  @JsonProperty("state")
  private State state;

  public int getReady() {
    return ready;
  }

  public void setReady(final int ready) {
    this.ready = ready;
  }

  public State getState() {
    return state;
  }

  public void setState(final State state) {
    this.state = state;
  }
}
