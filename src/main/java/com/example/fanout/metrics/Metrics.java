package com.example.fanout.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class Metrics {
  private final LongAdder ingested = new LongAdder();
  private final LongAdder dispatched = new LongAdder();
  private final Map<String, LongAdder> success = new ConcurrentHashMap<>();
  private final Map<String, LongAdder> failure = new ConcurrentHashMap<>();
  private final Map<String, LongAdder> retry = new ConcurrentHashMap<>();
  private final Instant start = Instant.now();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  public Metrics(int intervalSeconds) {
    scheduler.scheduleAtFixedRate(this::print, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
  }

  public void incrementIngested() {
    ingested.increment();
  }

  public void incrementDispatched() {
    dispatched.increment();
  }

  public void incrementSuccess(String sink) {
    success.computeIfAbsent(sink, k -> new LongAdder()).increment();
  }

  public void incrementFailure(String sink) {
    failure.computeIfAbsent(sink, k -> new LongAdder()).increment();
  }

  public void incrementRetry(String sink) {
    retry.computeIfAbsent(sink, k -> new LongAdder()).increment();
  }

  private void print() {
    long seconds = Math.max(1, Duration.between(start, Instant.now()).getSeconds());
    long processed = dispatched.sum();
    long throughput = processed / seconds;
    System.out.println("=== Status ===");
    System.out.println("Records ingested: " + ingested.sum());
    System.out.println("Records dispatched: " + processed);
    System.out.println("Throughput (records/sec): " + throughput);
    for (String sink : success.keySet()) {
      long ok = success.get(sink).sum();
      long fail = failure.getOrDefault(sink, new LongAdder()).sum();
      long retries = retry.getOrDefault(sink, new LongAdder()).sum();
      System.out.println("Sink " + sink + " -> success=" + ok + " failure=" + fail + " retries=" + retries);
    }
  }

  public void stop() {
    scheduler.shutdownNow();
    print();
  }
}
