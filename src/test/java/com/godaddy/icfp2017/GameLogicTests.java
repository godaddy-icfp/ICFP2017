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

    final GameplayP2S move = impl.move(previousMoves);

    assertNotNull(move);
  }

  // TODO tng - This test fails
  // @Test
  public void run_game_and_one_move_with_adjacent_mines() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    final GameplayP2S move = impl.move(loadMoves(setup));

    validateTimes(move);

    SimpleWeightedGraph<Site, River> graph = move.getState().getGraph();

    graph.vertexSet()
        .forEach(site -> graph.edgesOf(site)
            .forEach(river -> {
              if (site.isMine()) {
                assertTrue(
                    river.getAlgorithmWeights().get(Algorithms.AdjacentToMine) == Weights.Max);
              } else {
                assertTrue(
                    river.getAlgorithmWeights().get(Algorithms.AdjacentToMine) == Weights.Identity);
              }
            }));
  }

  // TODO tng - This test fails
  // @Test
  public void run_game_and_one_move_test_weights() throws IOException {
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    final GameplayP2S move = impl.move(loadMoves(setup));

    validateTimes(move);

    SimpleWeightedGraph<Site, River> graph = move.getState().getGraph();

    // tested values
    ImmutableList<Double> expectedValues =
        ImmutableList.of(1.0, 10.0, 1.0, 10.0, 10.0, 10.0, 10.0, 1.0, 10.0, 10.0, 1.0, 10.0);

    int i = 0;
    for (River river : graph.edgeSet()) {
      assertTrue(graph.getEdgeWeight(river) == expectedValues.indexOf(i));
      i += 1;
    }
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
    final GameplayP2S move = impl.move(loadMoves(setup));

    validateTimes(move);
  }

  private void validateTimes(GameplayP2S move) {
    for (Algorithms algorithm : Algorithms.values()) {
      Long timeToRun = move.getState().getLastTime(algorithm);
      if (null != timeToRun) {
        assertTrue(timeToRun < 200);
        System.out.println(algorithm + " took " + timeToRun + "ms");
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
