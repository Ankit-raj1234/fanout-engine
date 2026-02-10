package com.example.fanout.util;

import com.example.fanout.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class ConfigLoader {
  public static AppConfig load(String path) throws Exception {
    ObjectMapper mapper = path.endsWith(".yaml") || path.endsWith(".yml")
        ? new ObjectMapper(new YAMLFactory())
        : new ObjectMapper();
    return mapper.readValue(new File(path), AppConfig.class);
  }
}
