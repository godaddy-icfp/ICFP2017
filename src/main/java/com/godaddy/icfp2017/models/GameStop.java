package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class GameStop {

  @JsonProperty("moves")
  private List<Move> moves;

  public List<Move> getMoves() {
    return moves;
  }

  public void setMoves(final List<Move> moves) {
    this.moves = moves;
  }

  @JsonProperty("scores")
  private JsonNode scores;

  public JsonNode getScores() {
    return scores;
  }

  public void setScores(final JsonNode scores) {
    this.scores = scores;
  }
}
