package com.example.fanout.sink.impl;

import com.example.fanout.config.SinkConfig;
import com.example.fanout.transform.DbPayload;

public class WideColumnDbSink extends SimulatedSink<DbPayload> {
  public WideColumnDbSink(SinkConfig config) {
    super(config);
  }

  @Override
  protected void handle(DbPayload payload) {
    // simulate async UPSERT
  }
}
