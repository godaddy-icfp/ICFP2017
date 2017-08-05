package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphTests {
  private static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> getSampleGame() throws IOException {
    final ByteSource byteSource = Resources.asByteSource(Resources.getResource(
        GraphTests.class,
        "/SampleGame.json"));

    try (final InputStream resourceAsStream = byteSource.openStream()) {
      final SetupS2P setupS2P = JsonMapper.Instance.readValue(resourceAsStream, SetupS2P.class);
      return GameLogic.buildGraph(setupS2P);
    }
  }

  @Test
  public void build_simple_graph() throws IOException {
    final Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> tuple = getSampleGame();
    assertThat(tuple).isNotNull();
    assertThat(tuple.right).isNotNull();
  }

  @Test
  public void ensure_start_weights_are_identity() throws IOException {
    final Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> tuple = getSampleGame();
    final SimpleWeightedGraph<Site, River> graph = tuple.right;

    for (final River river : graph.edgeSet()) {
      final double edgeWeight = graph.getEdgeWeight(river);
      assertThat(edgeWeight).isEqualTo(Weights.Identity);
    }
  }

  @Test
  public void calculate_distance_to_mines() throws IOException {
    final Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> tuple = getSampleGame();
  }
}
