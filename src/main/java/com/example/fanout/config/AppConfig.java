package com.example.fanout.config;

import java.util.List;

public class AppConfig {
  public InputConfig input;
  public QueueConfig queue;
  public MetricsConfig metrics;
  public List<SinkConfig> sinks;
}
