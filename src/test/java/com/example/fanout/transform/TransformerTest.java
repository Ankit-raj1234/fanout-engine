package com.example.fanout.transform;

import com.example.fanout.core.SourceRecord;
import com.example.fanout.transform.impl.JsonTransformer;
import com.example.fanout.transform.impl.XmlTransformer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransformerTest {
  @Test
  void jsonTransformerProducesJson() {
    JsonTransformer t = new JsonTransformer();
    String out = t.transform(new SourceRecord(Map.of("id", "1", "name", "Ada")));
    assertTrue(out.contains("\"id\""));
  }

  @Test
  void xmlTransformerProducesXml() {
    XmlTransformer t = new XmlTransformer();
    String out = t.transform(new SourceRecord(Map.of("id", "1", "name", "Ada")));
    assertTrue(out.contains("<LinkedHashMap>"));
  }
}
