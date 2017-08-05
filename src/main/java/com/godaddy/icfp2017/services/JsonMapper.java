package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class JsonMapper extends ObjectMapper {
  public static final JsonMapper Instance = new JsonMapper();

  private JsonMapper() {
    registerModule(new GuavaModule());
  }
}
