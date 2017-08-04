package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class State {
  @JsonProperty
  private String foo;

  public String getFoo() {
    return foo;
  }

  public void setFoo(final String foo) {
    this.foo = foo;
  }
}
