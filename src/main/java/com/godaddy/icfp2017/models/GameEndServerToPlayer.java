package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"state"})
public class GameEndServerToPlayer implements ServerToPlayer {

  @JsonProperty("stop")
  private GameStop stop;

  public GameStop getStop() {
    return stop;
  }

  public void setStop(final GameStop stop) {
    this.stop = stop;
  }
}
