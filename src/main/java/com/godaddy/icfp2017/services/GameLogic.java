package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class GameLogic {

  private GameInitiator gameInitiator;

  private State currentState;
  private final PrintStream debugStream;

  public GameLogic(final PrintStream debugStream) {
    this.gameInitiator = new GameInitiator();
    this.debugStream = debugStream;
  }

  public SetupP2S setup(final SetupS2P setup) {
    this.currentState = gameInitiator.createState(setup);
    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(this.currentState);
    return response;
  }

  static Pair<ImmutableSet<Site>, SimpleWeightedGraph<Site, River>> buildGraph(
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

  public GameplayP2S move(final GameplayS2P move) {
    return new GameMove(this.currentState, this.debugStream).getMove(move);
  }
}
