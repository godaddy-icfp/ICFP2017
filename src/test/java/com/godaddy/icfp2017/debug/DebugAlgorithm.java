package com.godaddy.icfp2017.debug;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.models.serialization.KryoFactory;
import com.godaddy.icfp2017.services.GameAlgorithms;
import com.godaddy.icfp2017.services.GameDecision;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GameMove;
import com.godaddy.icfp2017.services.JsonMapper;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import net.jpountz.lz4.LZ4BlockInputStream;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Base64;
import java.util.EnumSet;

public class DebugAlgorithm {
  @Test(enabled = false)
  public void debug_algo() throws Exception {
    final State state = rehydrateState();
    final GameplayS2P moves = loadMoves();

    moves.setPreviousState(state);

    GameLogic logic = new DebugGameLogic();

    final GameplayP2S move = logic.move(moves);
  }

  private class DebugGameLogic extends GameLogic {
    public DebugGameLogic() {
      super(System.out);
    }

    @Override
    protected GameMove createGameMoves(
        final State currentState,
        final PrintStream debugStream, final GameAlgorithms algorithms) {
      return new GameMove(currentState, debugStream, algorithms, getDecisionDebug(debugStream));
    }

    @Override
    protected GameAlgorithms createGameAlgorithms(final EnumSet<Algorithms> selectedAlgorithms) {
      return super.createGameAlgorithms(selectedAlgorithms);
    }
  }

  private GameDecision getDecisionDebug(final PrintStream debugStream) {
    return new GameDecision(debugStream) {
      @Override
      public GameplayP2S getDecision(final State state) {
        return super.getDecision(state);
      }
    };
  }

  private GameplayS2P loadMoves() throws Exception {
    final ByteSource byteSource = Resources.asByteSource(Resources.getResource(
        DebugAlgorithm.class,
        "/debug/moves.json"));

    try (final InputStream resourceAsStream = byteSource.openStream()) {
      final GameplayS2P gameplayS2P = JsonMapper.Instance.readValue(resourceAsStream, GameplayS2P.class);
      return gameplayS2P;
    }
  }

  private State rehydrateState() throws Exception {
    final ByteSource byteSource = Resources.asByteSource(Resources.getResource(
        DebugAlgorithm.class,
        "/debug/State.b64"));
    final byte[] decode = Base64.getDecoder().decode(byteSource.read());
    try (final InputStream resourceAsStream = new ByteArrayInputStream(decode);
         LZ4BlockInputStream lz4 = new LZ4BlockInputStream(resourceAsStream)) {
      final Kryo kryo = KryoFactory.createKryo();
      return kryo.readObject(new Input(lz4), State.class);
    }
  }
}
