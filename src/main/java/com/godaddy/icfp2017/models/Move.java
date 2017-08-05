package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Move implements ICFPMessage {
  @JsonProperty("claim")
  private Claim claim;

  @JsonProperty("pass")
  private Pass pass;

  public Claim getClaim() {
    return claim;
  }

  public void setClaim(final Claim claim) {
    this.claim = claim;
  }

  public Pass getPass() {
    return pass;
  }

  public void setPass(final Pass pass) {
    this.pass = pass;
  }

}
