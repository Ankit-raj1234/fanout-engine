package com.example.fanout.core;

import com.example.fanout.config.AppConfig;
import com.example.fanout.util.ConfigLoader;
import org.junit.jupiter.api.Test;

public class EngineIntegrationTest {
  @Test
  void engineRunsWithSampleConfig() throws Exception {
    AppConfig config = ConfigLoader.load("application.yaml");
    Engine engine = new Engine(config);
    engine.start();
  }
}
