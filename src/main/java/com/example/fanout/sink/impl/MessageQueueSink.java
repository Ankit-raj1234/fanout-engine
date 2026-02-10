package com.example.fanout.sink.impl;

import com.example.fanout.config.SinkConfig;

public class MessageQueueSink extends SimulatedSink<String> {
  public MessageQueueSink(SinkConfig config) {
    super(config);
  }

  @Override
  protected void handle(String payload) {
    // simulate MQ publish
  }
}
