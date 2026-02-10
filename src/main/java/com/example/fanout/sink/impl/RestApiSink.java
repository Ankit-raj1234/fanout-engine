package com.example.fanout.sink.impl;

import com.example.fanout.config.SinkConfig;

public class RestApiSink extends SimulatedSink<String> {
  public RestApiSink(SinkConfig config) {
    super(config);
  }

  @Override
  protected void handle(String payload) {
    // simulate HTTP/2 POST
  }
}
