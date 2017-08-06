package com.godaddy.icfp2017;

import com.godaddy.icfp2017.models.*;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GraphTests;
import com.godaddy.icfp2017.services.JsonMapper;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

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
    Long timer = System.currentTimeMillis();
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadBigSetup());
    validateSetupTime(timer);

    final GameplayP2S move = impl.move(loadMoves(setup), null);

    validateTimes(move.getState());
  }

  private void validateSetupTime(Long timer) {
    Long timeTotal = System.currentTimeMillis() - timer;
    System.out.println("Setup for graph took " + timeTotal + "ms");
    assertTrue(timeTotal < 500);
  }

  @Test
  public void run_small_graph_test() throws IOException {
    Long timer = System.currentTimeMillis();
    GameLogic impl = new GameLogic();
    final SetupP2S setup = impl.setup(loadSetup());
    validateSetupTime(timer);

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

    @Test
    public void run_game_and_ensure_ranked_paths_are_calculated() throws IOException {
      GameLogic impl = new GameLogic();
      final SetupP2S setup = impl.setup(loadSetup());
      SortedSet<Path> actualRankedPaths = setup.getState().getRankedPaths();
      Set<Path> expectedRankedPaths = new HashSet<>();
      //expectedRankedPaths.add(new Path(5, 1, 8));
      expectedRankedPaths.add(new Path(1, 5, 8, 2));
/*      expectedRankedPaths.add(new Path(5, 2, 4));
      expectedRankedPaths.add(new Path(5, 0, 4));
      expectedRankedPaths.add(new Path(1, 6, 4));
      expectedRankedPaths.add(new Path(1, 4, 4));
      expectedRankedPaths.add(new Path(5, 7, 1));
      expectedRankedPaths.add(new Path(5, 6, 1));
      expectedRankedPaths.add(new Path(5, 4, 1));
      expectedRankedPaths.add(new Path(5, 3, 1));
      expectedRankedPaths.add(new Path(1, 7, 1));
      expectedRankedPaths.add(new Path(1, 3, 1));
      expectedRankedPaths.add(new Path(1, 2, 1));
      expectedRankedPaths.add(new Path(1, 0, 1));*/

      int size = actualRankedPaths.size();
      assertTrue(size == expectedRankedPaths.size());
      actualRankedPaths.addAll(expectedRankedPaths);
      assertTrue(actualRankedPaths.size() == size);

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
