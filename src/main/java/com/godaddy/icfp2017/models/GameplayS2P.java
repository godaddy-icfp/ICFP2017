package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameplayS2P implements ServerToPlayer {
  @JsonProperty("move")
  private PreviousMoves previousMoves;

  @JsonProperty("state")
  private State previousState;


  public PreviousMoves getPreviousMoves() {
    return previousMoves;
  }

  public void setPreviousMoves(final PreviousMoves previousMoves) {
    this.previousMoves = previousMoves;
  }

  public State getPreviousState() {
    return previousState;
  }

  public void setPreviousState(final State previousState) {
    this.previousState = previousState;
  }
}
