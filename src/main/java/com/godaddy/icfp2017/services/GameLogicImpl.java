package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.Map;
import com.godaddy.icfp2017.models.Pass;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class GameLogicImpl implements GameLogic {
  private State state;

  @Override
  public SetupP2S setup(final SetupS2P setup) {
    state = new State();

    // todo set some more initial state
    state.setPunter(setup.getPunter());

    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);

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

    state.setMap(builder.build());

    return response;
  }

  @Override
  public GameplayP2S move(final GameplayS2P move) {
    // load previous state
    final State previousState = move.getPreviousState();
    if (previousState != null) {
      this.state = previousState;
    }

    Pass pass = new Pass();
    pass.setPunter(state.getPunter());

    // todo initialize a reasonable claim
    //    Claim claim = new Claim();
    //    claim.setPunter(state.getPunter());
    //    claim.setTarget(0);
    //    claim.setSource(0);
    //    response.setClaim(claim);

    // initialize the response
    final GameplayP2S response = new GameplayP2S();
    response.setPass(pass); // todo change this to setClaim
    response.setState(state);
    return response;
  }
}
