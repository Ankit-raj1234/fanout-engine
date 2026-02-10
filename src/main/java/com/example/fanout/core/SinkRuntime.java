package com.example.fanout.core;

import com.example.fanout.config.SinkConfig;
import com.example.fanout.metrics.Metrics;
import com.example.fanout.sink.Sink;
import com.example.fanout.transform.Transformer;
import com.example.fanout.util.DeadLetterQueue;

import java.util.concurrent.*;

public class SinkRuntime<T> {
  private final SinkConfig config;
  private final Sink<T> sink;
  private final Transformer<T> transformer;
  private final Metrics metrics;
  private final BlockingQueue<SourceRecord> queue;
  private final CountDownLatch done;
  private volatile boolean running = true;
  private final DeadLetterQueue dlq;

  public SinkRuntime(SinkConfig config, Sink<T> sink, Transformer<T> transformer, Metrics metrics) {
    this.config = config;
    this.sink = sink;
    this.transformer = transformer;
    this.metrics = metrics;
    this.queue = new ArrayBlockingQueue<>(config.queueSize);
    this.done = new CountDownLatch(config.workers);
    this.dlq = new DeadLetterQueue("dlq/" + config.name + ".jsonl");
  }

  public void start(ExecutorService executor) {
    for (int i = 0; i < config.workers; i++) {
      executor.submit(this::runWorker);
    }
  }

  public void enqueue(SourceRecord record) throws InterruptedException {
    queue.put(record);
  }

  public void stop() {
    running = false;
  }

  private void runWorker() {
    try {
      while (running || !queue.isEmpty()) {
        SourceRecord record = queue.poll(200, TimeUnit.MILLISECONDS);
        if (record == null) {
          continue;
        }
        T payload = transformer.transform(record);
        boolean success = sendWithRetry(payload);
        if (success) {
          metrics.incrementSuccess(config.name);
        } else {
          metrics.incrementFailure(config.name);
          dlq.write(record.data);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      done.countDown();
    }
  }

  private boolean sendWithRetry(T payload) {
    int maxRetries = Math.min(config.maxRetries, 3);
    int attempts = 0;
    while (attempts < maxRetries) {
      attempts++;
      try {
        sink.send(payload);
        return true;
      } catch (Exception ex) {
        metrics.incrementRetry(config.name);
        try {
          Thread.sleep(100L * attempts);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return false;
        }
      }
    }
    return false;
  }

  public void awaitCompletion() throws InterruptedException {
    done.await();
  }
}
