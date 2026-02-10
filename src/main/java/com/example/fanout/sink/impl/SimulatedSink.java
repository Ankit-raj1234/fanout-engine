package com.example.fanout.sink.impl;

import com.example.fanout.config.SinkConfig;
import com.example.fanout.sink.Sink;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Random;

public abstract class SimulatedSink<T> implements Sink<T> {
  protected final SinkConfig config;
  private final RateLimiter limiter;
  private final Random random = new Random();

  protected SimulatedSink(SinkConfig config) {
    this.config = config;
    this.limiter = RateLimiter.create(config.rateLimitPerSec);
  }

  @Override
  public String name() {
    return config.name;
  }

  @Override
  public void send(T payload) throws Exception {
    limiter.acquire();
    if (config.latencyMs > 0) {
      Thread.sleep(config.latencyMs);
    }
    if (random.nextDouble() < config.errorRate) {
      throw new RuntimeException("Simulated failure in " + config.name);
    }
    handle(payload);
  }

  protected abstract void handle(T payload);
}
