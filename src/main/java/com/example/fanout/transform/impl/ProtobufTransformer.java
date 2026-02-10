package com.example.fanout.transform.impl;

import com.example.fanout.core.SourceRecord;
import com.example.fanout.transform.Transformer;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class ProtobufTransformer implements Transformer<byte[]> {
  @Override
  public byte[] transform(SourceRecord record) {
    Map<String, Object> data = record.data;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      CodedOutputStream cos = CodedOutputStream.newInstance(out);

      String id = valueOrNull(data.get("id"));
      String name = valueOrNull(data.get("name"));
      String email = valueOrNull(data.get("email"));
      Long ts = parseLongOrNull(data.get("timestamp"));

      if (id != null) cos.writeString(1, id);
      if (name != null) cos.writeString(2, name);
      if (email != null) cos.writeString(3, email);
      if (ts != null) cos.writeInt64(4, ts);

      Object attrs = data.get("attributes");
      if (attrs instanceof Map<?, ?> map) {
        for (Map.Entry<?, ?> e : map.entrySet()) {
          String key = String.valueOf(e.getKey());
          String value = String.valueOf(e.getValue());
          int entrySize = CodedOutputStream.computeStringSize(1, key)
              + CodedOutputStream.computeStringSize(2, value);
          cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
          cos.writeUInt32NoTag(entrySize);
          cos.writeString(1, key);
          cos.writeString(2, value);
        }
      }

      cos.flush();
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String valueOrNull(Object v) {
    if (v == null) return null;
    String s = String.valueOf(v);
    return s.isBlank() ? null : s;
  }

  private static Long parseLongOrNull(Object v) {
    if (v == null) return null;
    try {
      return Long.parseLong(String.valueOf(v));
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
