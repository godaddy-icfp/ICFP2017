package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PreviousMoves {
  @JsonProperty("moves")
  private List<Move> moves;

  public List<Move> getMoves() {
    return moves;
  }

  public void setMoves(final List<Move> moves) {
    this.moves = moves;
  }
}
