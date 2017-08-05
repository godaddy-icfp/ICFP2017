package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.annotation.JsonValue;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import org.jgrapht.EdgeFactory;

import java.io.Serializable;

class LambdaEdgeFactory implements EdgeFactory<Site, River>, Serializable {
  @JsonValue
  private int foo = 1; // leave this dumb thing for now, it fixes jackson serialization errors

  @Override
  public River createEdge(final Site sourceVertex, final Site targetVertex) {
    final River river = new River();
    river.setSource(sourceVertex.getId());
    river.setTarget(targetVertex.getId());
    return river;
  }
}
