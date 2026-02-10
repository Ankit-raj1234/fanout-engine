package com.example.fanout.transform.impl;

import com.example.fanout.core.SourceRecord;
import com.example.fanout.transform.Transformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTransformer implements Transformer<String> {
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String transform(SourceRecord record) {
    try {
      return mapper.writeValueAsString(record.data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
