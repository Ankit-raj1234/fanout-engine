package com.example.fanout.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadLetterQueue {
  private final ObjectMapper mapper = new ObjectMapper();
  private final File file;
  private final Lock lock = new ReentrantLock();

  public DeadLetterQueue(String path) {
    this.file = new File(path);
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
  }

  public void write(Map<String, Object> record) {
    lock.lock();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
      writer.write(mapper.writeValueAsString(record));
      writer.newLine();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }
}
