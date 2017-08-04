package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.State;

public class GameLogicImpl implements GameLogic {
  @Override
  public SetupP2S setup(final SetupS2P setup) {
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(new State());
    return response;
  }

  @Override
  public GameplayP2S move(final GameplayS2P move) {
    final GameplayP2S response = new GameplayP2S();
    return response;
  }
}
