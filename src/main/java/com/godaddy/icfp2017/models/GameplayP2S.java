package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameplayP2S extends Move implements PlayerToServer {
  @JsonProperty("state")
  private State state;

  public State getState() {
    return state;
  }

  public void setState(final State state) {
    this.state = state;
  }
}
