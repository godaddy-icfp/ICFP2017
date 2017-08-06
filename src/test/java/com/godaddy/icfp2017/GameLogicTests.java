package com.godaddy.icfp2017;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GraphTests;
import com.godaddy.icfp2017.services.JsonMapper;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import com.google.common.collect.ImmutableList;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

public class GameLogicTests {

  @Test
  public void run_game_and_one_move() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    final GameplayS2P previousMoves = loadMoves(setup);

    final GameplayP2S move = impl.move(previousMoves, null);

    assertNotNull(move);
  }

  @Test
  public void run_game_and_one_move_with_adjacent_mines() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    State state = setup.getState();
    final GraphAlgorithm graphAlgorithm = impl.getGraphAlgorithm(Algorithms.AdjacentToMine);
    graphAlgorithm.run(Algorithms.AdjacentToMine, state);

    validateTimes(state);

    SimpleWeightedGraph<Site, River> graph = state.getGraph();

    graph.vertexSet()
        .forEach(site -> graph.edgesOf(site)
            .forEach(river -> {
              Double weight = river.getAlgorithmWeights().get(Algorithms.AdjacentToMine);
              if (site.isMine()) {
                assertTrue(
                     weight == Weights.Max);
              }
            }));
  }

  private SetupS2P loadBigSetup() throws IOException {
    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleBigGame.json");

    return JsonMapper.Instance.readValue(resourceAsStream, SetupS2P.class);
  }

  @Test
  public void run_large_graph_test() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadBigSetup());
    final GameplayP2S move = impl.move(loadMoves(setup), null);

    validateTimes(move.getState());
  }

  @Test
  public void run_small_graph_test() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    final GameplayP2S move = impl.move(loadMoves(setup), null);

    validateTimes(move.getState());
  }

  private void validateTimes(State state) {
    for (Algorithms algorithm : Algorithms.values()) {
      Long timeToRun = state.getLastTime(algorithm);
      if (null != timeToRun) {
        System.out.println(algorithm + " took " + timeToRun + "ms");
        assertTrue(timeToRun < 200);
      }
    }
  }

  @Test
  public void run_() throws IOException {

    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());

    final String s = JsonMapper.Instance.writeValueAsString(setup.getState());

    assertNotNull(s);

    final State state = JsonMapper.Instance.readValue(s, State.class);

    assertNotNull(state);
  }

  private SetupS2P loadSetup() throws IOException {

    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleGame.json");

    return JsonMapper.Instance.readValue(resourceAsStream, SetupS2P.class);
  }

  private GameplayS2P loadMoves(final SetupP2S setup) throws IOException {

    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleMoves.json");

    final GameplayS2P moves = JsonMapper.Instance.readValue(resourceAsStream, GameplayS2P.class);

    moves.setPreviousState(setup.getState());

    return moves;
  }
}
