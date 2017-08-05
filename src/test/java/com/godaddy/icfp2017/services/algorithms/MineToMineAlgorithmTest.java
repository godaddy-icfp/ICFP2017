package com.godaddy.icfp2017.services.algorithms;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MineToMineAlgorithmTest {
  @Test
  public void testPathWeight() throws Exception {
    assertEquals(
        MineToMineAlgorithm.pathWeight(2.0, 2.0, 4.0),
        1.05833,
        1e-3);

    assertEquals(
        MineToMineAlgorithm.pathWeight(1.0, 2.0, 4.0),
        1.11666,
        1e-3);

    assertEquals(
        MineToMineAlgorithm.pathWeight(1.0, 32.0, 32.0),
        1.67878,
        1e-3);

    assertEquals(
        MineToMineAlgorithm.pathWeight(0.0, 32.0, 32.0),
        1.7,
        1e-9);
  }
}