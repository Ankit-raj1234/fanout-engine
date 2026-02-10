package com.example.fanout.config;

public class SinkConfig {
  public String name;
  public String type; // REST, GRPC, MQ, WIDE_DB
  public int workers = 1;
  public int queueSize = 1000;
  public double rateLimitPerSec = 100.0;
  public int maxRetries = 3;
  public int latencyMs = 10;
  public double errorRate = 0.01; // simulated failure rate
}
