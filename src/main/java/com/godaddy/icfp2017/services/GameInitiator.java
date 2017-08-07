package com.godaddy.icfp2017.services;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.godaddy.icfp2017.models.Map;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.analysis.MineToMinePathAnalyzer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.function.Function;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

public class GameInitiator {

  public State createState(SetupS2P setup) {
    final State state = new State();

    state.setPunter(setup.getPunter());
    state.setPunters(setup.getPunters());

    final GraphConstruction graphConstruction = buildGraphs(setup);
    state.setGraph(graphConstruction.graph);
    state.setGraphOfEnemyMoves(graphConstruction.graphOfEnemyMoves);
    state.setMines(graphConstruction.mines);
    state.setSiteToMap(graphConstruction.siteToMap);
    state.setShortestPaths(new FloydWarshallShortestPaths<>(graphConstruction.graph));

    new MineToMinePathAnalyzer().analyze(state);
    RankedPathsCalculator calculator = new RankedPathsCalculator(state);
    state.setRankedPaths(calculator.calculate());

    return state;
  }

  private static GraphConstruction buildGraphs(final SetupS2P setup) {
    final Map map = setup.getMap();
    final List<Site> sites = map.getSites();
    final List<River> rivers = map.getRivers();
    final ImmutableSet<Integer> mines = ImmutableSet.copyOf(map.getMines());
    final ImmutableMap<Integer, Site> siteById = sites
        .stream()
        .collect(toImmutableMap(Site::getId, Function.identity()));

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?> builder =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    final UndirectedWeightedGraphBuilderBase<Site, River, ? extends SimpleWeightedGraph<Site, River>, ?>
        builderOfEnemyMoves =
        SimpleWeightedGraph.builder(new LambdaEdgeFactory());

    for (final Site site : sites) {
      site.setMine(mines.contains(site.getId()));
      builder.addVertex(site);
      builderOfEnemyMoves.addVertex(site);
    }

    for (final River river : rivers) {
      builder.addEdge(
          siteById.get(river.getSource()),
          siteById.get(river.getTarget()),
          river);
    }

    return new GraphConstruction(
        mines.stream().map(siteById::get).collect(toImmutableSet()),
        builder.build(),
        builderOfEnemyMoves.build(),
        siteById);
  }
}
