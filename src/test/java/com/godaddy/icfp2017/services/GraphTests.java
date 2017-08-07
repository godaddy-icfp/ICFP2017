package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.Map;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.util.List;
import java.util.function.Function;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphTests {
  private static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> getSampleGame() throws IOException {
    final ByteSource byteSource = Resources.asByteSource(Resources.getResource(
        GraphTests.class,
        "/SampleGame.json"));

    try (final InputStream resourceAsStream = byteSource.openStream()) {
      final SetupS2P setupS2P = JsonMapper.Instance.readValue(resourceAsStream, SetupS2P.class);
      return buildGraph(setupS2P);
    }
  }

  private static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> buildGraph(
      final SetupS2P setup) {
    final Map map = setup.getMap();
    final List<Site> sites = map.getSites();
    final List<River> rivers = map.getRivers();
    final ImmutableSet<Integer> mines = ImmutableSet.copyOf(map.getMines());
    final ImmutableMap<Integer, Site> siteById = sites
        .stream()
        .collect(toImmutableMap(Site::getId, Function.identity()));

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?> builder =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    for (final Site site : sites) {
      site.setMine(mines.contains(site.getId()));
      builder.addVertex(site);
    }

    for (final River river : rivers) {
      builder.addEdge(
          siteById.get(river.getSource()),
          siteById.get(river.getTarget()),
          river);
    }

    return Pair.of(
        mines.stream().map(siteById::get).collect(toImmutableSet()),
        builder.build());
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
      assertThat(edgeWeight).isEqualTo(1.0);
    }
  }

  @Test
  public void calculate_distance_to_mines() throws IOException {
    final Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> tuple = getSampleGame();
  }
}
