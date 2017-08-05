package com.godaddy.icfp2017;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.services.GameLogicImpl;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

public class GraphTests {
  @Test
  public void build_simple_graph() throws IOException {
    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleGame.json");

    final SetupS2P setupS2P = new ObjectMapper().readValue(resourceAsStream, SetupS2P.class);

    final SimpleWeightedGraph<Site, River> siteRiverSimpleWeightedGraph = GameLogicImpl.buildGraph(setupS2P);

  }

  @Test
  public void () throws IOException {
    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleGame.json");

    final SetupS2P setupS2P = new ObjectMapper().readValue(resourceAsStream, SetupS2P.class);

    final SimpleWeightedGraph<Site, River> siteRiverSimpleWeightedGraph = GameLogicImpl.buildGraph(setupS2P);

  }
}
