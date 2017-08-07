package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameEndServerToPlayer;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.ICFPMessage;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;

class TestHandler implements StateMachine.Handler<Void> {
  @Override
  public void capture(final ICFPMessage message) {
  }

  @Override
  public void debug(final String s) {
  }

  @Override
  public SetupP2S setup(final SetupS2P message) {
    return null;
  }

  @Override
  public GameplayP2S gameplay(final GameplayS2P message) {
    return null;
  }

  @Override
  public void timeout() {
  }

  @Override
  public Void stop(final GameEndServerToPlayer message) {
    return null;
  }
}
