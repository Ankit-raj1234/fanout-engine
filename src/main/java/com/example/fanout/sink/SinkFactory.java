package com.example.fanout.sink;

import com.example.fanout.config.SinkConfig;
import com.example.fanout.sink.impl.*;

public class SinkFactory {
  public static Sink<?> create(SinkConfig config) {
    return switch (config.type) {
      case "REST" -> new RestApiSink(config);
      case "GRPC" -> new GrpcSink(config);
      case "MQ" -> new MessageQueueSink(config);
      case "WIDE_DB" -> new WideColumnDbSink(config);
      default -> throw new IllegalArgumentException("Unknown sink type: " + config.type);
    };
  }
}
