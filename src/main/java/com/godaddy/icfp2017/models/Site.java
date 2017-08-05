package com.godaddy.icfp2017.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Site {
  @JsonProperty("id")
  private int id;
  private boolean isMine;

  public Site() {}

  public Site(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Site && ((Site) obj).id == id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, isMine);
  }

  @Override
  public String toString() {
    return Integer.toString(id);
  }

  public boolean isMine() {
    return isMine;
  }

  public void setMine(final boolean mine) {
    isMine = mine;
  }
}
