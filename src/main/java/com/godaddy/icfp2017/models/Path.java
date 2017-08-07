package com.godaddy.icfp2017.models;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

public class Path implements Comparable<Path>, Serializable {
  private static final long serialVersionUID = 42L;

  private int source;
  private int target;
  private int score;
  private int length;

  public Path(final int source, final int target, final int score, final int length) {
    this.source = source;
    this.target = target;
    this.score = score;
    this. length = length;
  }

  public Path() {}

  public int getSource() { return source; }
  public int getTarget() { return target; }
  public int getScore() { return score; }
  public int getLength() { return length; }

  @Override
  public int compareTo(Path p) {
    int weightComparison = Integer.compare(p.getScore(), score);
    if (weightComparison == 0) {
      int sourceComparison = Integer.compare(p.getSource(), source);
      if (sourceComparison == 0) {
        return Integer.compare(p.getTarget(), target);
      }
      else{
        return sourceComparison;
      }
    }
    else {
      return weightComparison;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Path &&
        ((Path) obj).source == source &&
        ((Path) obj).target == target &&
        ((Path) obj).score == score &&
        ((Path) obj).length == length;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source, target, score, length);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("source", source)
        .add("target", target)
        .add("score", score)
        .add("length", length)
        .toString();
  }
}

