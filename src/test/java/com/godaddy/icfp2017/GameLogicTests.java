package com.godaddy.icfp2017;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.Path;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.GameAlgorithms;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GraphTests;
import com.godaddy.icfp2017.services.JsonMapper;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadSetup());
    final GameplayS2P previousMoves = loadMoves(setup);

    final GameplayP2S move = impl.move(previousMoves);

    assertNotNull(move);
  }

  @Test
  public void run_game_and_one_move_with_adjacent_mines() throws IOException {
    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadSetup());
    State state = setup.getState();
    final GameAlgorithms gameAlgorithms = new GameAlgorithms(new PrintStream(new ByteArrayOutputStream()));
    final GraphAlgorithm graphAlgorithm = gameAlgorithms.getGraphAlgorithm(Algorithms.AdjacentToMine);
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

  @Test
  public void run_large_graph_test() throws IOException {
    Long timer = System.currentTimeMillis();
    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadBigSetup());
    validateSetupTime(timer);

    final GameplayP2S move = impl.move(loadMoves(setup));

    validateTimes(move.getState());
  }

  @Test
  public void run_small_graph_test() throws IOException {
    Long timer = System.currentTimeMillis();
    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadSetup());
    validateSetupTime(timer);

    final GameplayP2S move = impl.move(loadMoves(setup));

    validateTimes(move.getState());
  }

  @Test
  public void verify_weights_are_set() throws IOException {
    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadBigSetup());
    State state = setup.getState();
    final GameAlgorithms gameAlgorithms = new GameAlgorithms(new PrintStream(new ByteArrayOutputStream()));
    for (Algorithms algorithm : Algorithms.values()) {
      if (gameAlgorithms.isUsingAlgorithm(algorithm)) {
    final GraphAlgorithm graphAlgorithm = gameAlgorithms.getGraphAlgorithm(algorithm);
        // Remove all current weights
        state.getGraph().edgeSet().forEach(river -> river.getAlgorithmWeights().clear());

        graphAlgorithm.run(Algorithms.AdjacentToMine, state);

        // Verify the algorithm sets a valid weight on each river explicitly
        System.out.println("Validating weight settings for " + algorithm.toString());
        state.getGraph().edgeSet()
             .forEach(river -> {
               assertTrue(river.getAlgorithmWeights().size() > 0);
               river.getAlgorithmWeights().values().forEach(weight -> {
                 assertTrue(weight <= 1.0);
                 assertTrue(weight > 0.0);
               });
             });
      }
    }
  }

  private void validateSetupTime(Long timer) {
    Long timeTotal = System.currentTimeMillis() - timer;
    System.out.println("Setup for graph took " + timeTotal + "ms");
    assertTrue(timeTotal < 500);
  }

  private void validateTimes(State state) {
    for (String key : state.getLastTimes().keySet()) {
      Long timeToRun = state.getLastTimes().get(key);
      if (null != timeToRun) {
        System.out.println(key + " took " + timeToRun + "ms");
        assertTrue(timeToRun < 200);
      }
    }
  }

  @Test
  public void run_() throws IOException {

    GameLogic impl = new GameLogic(System.err);
    final SetupP2S setup = impl.setup(loadSetup());

    final String s = JsonMapper.Instance.writeValueAsString(setup.getState());

    assertNotNull(s);

    final State state = JsonMapper.Instance.readValue(s, State.class);

    assertNotNull(state);
  }

  @Test
  public void run_game_and_ensure_ranked_paths_are_calculated() throws IOException {
    GameLogic impl = new GameLogic(System.err);
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

  private SetupS2P loadBigSetup() throws IOException {
    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleBigGame.json");

    return JsonMapper.Instance.readValue(resourceAsStream, SetupS2P.class);
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
