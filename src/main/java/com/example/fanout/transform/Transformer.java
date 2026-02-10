package com.example.fanout.transform;

import com.example.fanout.core.SourceRecord;

public interface Transformer<T> {
  T transform(SourceRecord record);
}
