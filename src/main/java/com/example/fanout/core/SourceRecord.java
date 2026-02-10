package com.example.fanout.core;

import java.util.Map;

public class SourceRecord {
  public final Map<String, Object> data;

  public SourceRecord(Map<String, Object> data) {
    this.data = data;
  }
}
