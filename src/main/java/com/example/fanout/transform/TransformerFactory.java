package com.example.fanout.transform;

import com.example.fanout.transform.impl.*;

public class TransformerFactory {
  public static Transformer<?> create(String sinkType) {
    return switch (sinkType) {
      case "REST" -> new JsonTransformer();
      case "GRPC" -> new ProtobufTransformer();
      case "MQ" -> new XmlTransformer();
      case "WIDE_DB" -> new WideDbTransformer();
      default -> throw new IllegalArgumentException("Unknown sink type: " + sinkType);
    };
  }
}
