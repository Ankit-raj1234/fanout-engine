package com.example.fanout.config;

import java.util.List;

public class InputConfig {
  public String path;
  public String format; // CSV, JSONL, FIXED
  public String delimiter = ",";
  public List<FixedWidthColumn> fixedWidthColumns;
}
