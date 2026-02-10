package com.example.fanout.core;

import com.example.fanout.config.AppConfig;
import com.example.fanout.config.MetricsConfig;
import com.example.fanout.config.QueueConfig;
import com.example.fanout.config.SinkConfig;
import com.example.fanout.metrics.Metrics;
import com.example.fanout.sink.Sink;
import com.example.fanout.sink.SinkFactory;
import com.example.fanout.transform.Transformer;
import com.example.fanout.transform.TransformerFactory;
import com.example.fanout.util.InputReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Engine {
  private final AppConfig config;

  public Engine(AppConfig config) {
    this.config = config;
  }

  public void start() throws Exception {
    QueueConfig queueCfg = config.queue != null ? config.queue : new QueueConfig();
    MetricsConfig metricsCfg = config.metrics != null ? config.metrics : new MetricsConfig();

    Metrics metrics = new Metrics(metricsCfg.intervalSeconds);
    BlockingQueue<SourceRecord> sourceQueue = new ArrayBlockingQueue<>(queueCfg.sourceQueueSize);

    InputReader reader = new InputReader(config.input);
    Producer producer = new Producer(reader, sourceQueue, metrics);

    List<SinkRuntime<?>> runtimes = new ArrayList<>();
    for (SinkConfig sinkConfig : config.sinks) {
      Sink<?> sink = SinkFactory.create(sinkConfig);
      Transformer<?> transformer = TransformerFactory.create(sinkConfig.type);
      SinkRuntime<?> runtime = new SinkRuntime<>(sinkConfig, sink, transformer, metrics);
      runtimes.add(runtime);
    }

    Dispatcher dispatcher = new Dispatcher(sourceQueue, runtimes, metrics);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      executor.submit(producer);
      executor.submit(dispatcher);
      for (SinkRuntime<?> runtime : runtimes) {
        runtime.start(executor);
      }

      producer.awaitCompletion();
      dispatcher.awaitCompletion();
      for (SinkRuntime<?> runtime : runtimes) {
        runtime.awaitCompletion();
      }
      metrics.stop();
    }
  }
}
