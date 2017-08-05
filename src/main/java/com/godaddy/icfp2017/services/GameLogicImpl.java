package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.Claim;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.Pass;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.State;

public class GameLogicImpl implements GameLogic {
  private State state;

  @Override
  public SetupP2S setup(final SetupS2P setup) {
    state = new State();

    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(state);
    return response;
  }

  @Override
  public GameplayP2S move(final GameplayS2P move) {
    final GameplayP2S response = new GameplayP2S();

    Pass pass = new Pass();
    pass.setPunter(state.getPunter());
    response.setPass(pass);

    // todo
//    Claim claim = new Claim();
//    claim.setPunter(state.getPunter());
//    claim.setTarget(0);
//    claim.setSource(0);
//    response.setClaim(claim);

    response.setState(state);
    return response;
  }
}
