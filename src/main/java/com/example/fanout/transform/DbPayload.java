package com.example.fanout.transform;

import java.util.Map;

public class DbPayload {
  public final Map<String, Object> cqlMap;
  public final byte[] avroBytes;

  public DbPayload(Map<String, Object> cqlMap, byte[] avroBytes) {
    this.cqlMap = cqlMap;
    this.avroBytes = avroBytes;
  }
}
