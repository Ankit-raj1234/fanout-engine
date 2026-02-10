package com.example.fanout.core;

import com.example.fanout.metrics.Metrics;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Dispatcher implements Runnable {
  private final BlockingQueue<SourceRecord> in;
  private final List<SinkRuntime<?>> sinks;
  private final Metrics metrics;
  private final CountDownLatch done = new CountDownLatch(1);

  public Dispatcher(BlockingQueue<SourceRecord> in, List<SinkRuntime<?>> sinks, Metrics metrics) {
    this.in = in;
    this.sinks = sinks;
    this.metrics = metrics;
  }

  @Override
  public void run() {
    try {
      while (true) {
        SourceRecord record = in.take();
        if (record == Producer.poisonPill()) {
          break;
        }
        for (SinkRuntime<?> sink : sinks) {
          sink.enqueue(record);
        }
        metrics.incrementDispatched();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      for (SinkRuntime<?> sink : sinks) {
        sink.stop();
      }
      done.countDown();
    }
  }

  public void awaitCompletion() throws InterruptedException {
    done.await();
  }
}
