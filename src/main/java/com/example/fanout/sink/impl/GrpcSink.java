package com.example.fanout.sink.impl;

import com.example.fanout.config.SinkConfig;

public class GrpcSink extends SimulatedSink<byte[]> {
  public GrpcSink(SinkConfig config) {
    super(config);
  }

  @Override
  protected void handle(byte[] payload) {
    // simulate gRPC send
  }
}
