package com.example.fanout.core;

import com.example.fanout.metrics.Metrics;
import com.example.fanout.util.InputReader;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Producer implements Runnable {
  private final InputReader reader;
  private final BlockingQueue<SourceRecord> out;
  private final Metrics metrics;
  private final CountDownLatch done = new CountDownLatch(1);
  private static final SourceRecord POISON = new SourceRecord(java.util.Map.of("__POISON__", true));

  public Producer(InputReader reader, BlockingQueue<SourceRecord> out, Metrics metrics) {
    this.reader = reader;
    this.out = out;
    this.metrics = metrics;
  }

  @Override
  public void run() {
    try {
      reader.forEach(record -> {
        try {
          out.put(record);
          metrics.incrementIngested();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    } finally {
      try {
        out.put(POISON);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      done.countDown();
    }
  }

  public void awaitCompletion() throws InterruptedException {
    done.await();
  }

  public static SourceRecord poisonPill() {
    return POISON;
  }
}
