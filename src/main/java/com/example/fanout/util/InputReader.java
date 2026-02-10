package com.example.fanout.util;

import com.example.fanout.config.FixedWidthColumn;
import com.example.fanout.config.InputConfig;
import com.example.fanout.core.SourceRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class InputReader {
  private final InputConfig config;
  private final ObjectMapper mapper = new ObjectMapper();

  public InputReader(InputConfig config) {
    this.config = config;
  }

  public void forEach(Consumer<SourceRecord> consumer) {
    switch (config.format) {
      case "CSV" -> readCsv(consumer);
      case "JSONL" -> readJsonl(consumer);
      case "FIXED" -> readFixedWidth(consumer);
      default -> throw new IllegalArgumentException("Unsupported format: " + config.format);
    }
  }

  private void readCsv(Consumer<SourceRecord> consumer) {
    try (BufferedReader reader = Files.newBufferedReader(new File(config.path).toPath(), StandardCharsets.UTF_8)) {
      char delim = (config.delimiter == null || config.delimiter.isEmpty()) ? ',' : config.delimiter.charAt(0);
      CSVParser parser = CSVFormat.DEFAULT
          .withFirstRecordAsHeader()
          .withDelimiter(delim)
          .parse(reader);
      for (CSVRecord record : parser) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String header : parser.getHeaderMap().keySet()) {
          map.put(header, record.get(header));
        }
        consumer.accept(new SourceRecord(map));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void readJsonl(Consumer<SourceRecord> consumer) {
    try (BufferedReader reader = Files.newBufferedReader(new File(config.path).toPath(), StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) continue;
        Map<String, Object> map = mapper.readValue(line, new TypeReference<>() {});
        consumer.accept(new SourceRecord(map));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void readFixedWidth(Consumer<SourceRecord> consumer) {
    if (config.fixedWidthColumns == null || config.fixedWidthColumns.isEmpty()) {
      throw new IllegalArgumentException("fixedWidthColumns required for FIXED format");
    }
    try (BufferedReader reader = Files.newBufferedReader(new File(config.path).toPath(), StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (FixedWidthColumn col : config.fixedWidthColumns) {
          int start = Math.max(0, col.start);
          int end = Math.min(line.length(), col.end);
          String value = start >= line.length() ? "" : line.substring(start, end).trim();
          map.put(col.name, value);
        }
        consumer.accept(new SourceRecord(map));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
