package com.example.fanout;

import com.example.fanout.config.AppConfig;
import com.example.fanout.core.Engine;
import com.example.fanout.util.ConfigLoader;

public class App {
  public static void main(String[] args) throws Exception {
    String configPath = args.length > 0 ? args[0] : "application.yaml";
    AppConfig config = ConfigLoader.load(configPath);
    Engine engine = new Engine(config);
    engine.start();
  }
}
