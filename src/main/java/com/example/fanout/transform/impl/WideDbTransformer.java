package com.example.fanout.transform.impl;

import com.example.fanout.core.SourceRecord;
import com.example.fanout.transform.DbPayload;
import com.example.fanout.transform.Transformer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.generic.GenericDatumWriter;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class WideDbTransformer implements Transformer<DbPayload> {
  private static final Schema SCHEMA = new Schema.Parser().parse(
      "{\"type\":\"record\",\"name\":\"Record\",\"fields\":[" +
      "{\"name\":\"id\",\"type\":\"string\"}," +
      "{\"name\":\"name\",\"type\":[\"null\",\"string\"],\"default\":null}," +
      "{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}," +
      "{\"name\":\"timestamp\",\"type\":[\"null\",\"long\"],\"default\":null}" +
      "]}");

  @Override
  public DbPayload transform(SourceRecord record) {
    Map<String, Object> map = record.data;
    byte[] avroBytes = toAvro(map);
    return new DbPayload(map, avroBytes);
  }

  private byte[] toAvro(Map<String, Object> map) {
    try {
      GenericRecord rec = new GenericData.Record(SCHEMA);
      rec.put("id", String.valueOf(map.getOrDefault("id", "")));
      rec.put("name", map.get("name") == null ? null : String.valueOf(map.get("name")));
      rec.put("email", map.get("email") == null ? null : String.valueOf(map.get("email")));
      if (map.get("timestamp") != null) {
        try {
          rec.put("timestamp", Long.parseLong(String.valueOf(map.get("timestamp"))));
        } catch (NumberFormatException e) {
          rec.put("timestamp", null);
        }
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(SCHEMA);
      BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
      writer.write(rec, encoder);
      encoder.flush();
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
