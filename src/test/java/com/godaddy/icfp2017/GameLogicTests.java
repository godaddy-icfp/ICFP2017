package com.godaddy.icfp2017;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.services.GameLogic;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class GameLogicTests {
  @Test
  public void run_game_and_one_move() throws IOException {
    GameLogic impl = new GameLogic();
    impl.setup(loadSetup());
    final GameplayP2S move = impl.move(loadMoves());

    assertThat(move).isNotNull();
  }

  private SetupS2P loadSetup() throws IOException {

    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleGame.json");

    final SetupS2P setupS2P = new ObjectMapper().readValue(resourceAsStream, SetupS2P.class);
    return setupS2P;
  }

  private GameplayS2P loadMoves() throws IOException {

    final ClassLoader classLoader = GraphTests.class.getClassLoader();
    final InputStream resourceAsStream = classLoader.getResourceAsStream("SampleMoves.json");

    final GameplayS2P setupS2P = new ObjectMapper().readValue(resourceAsStream, GameplayS2P.class);
    return setupS2P;
  }
}
