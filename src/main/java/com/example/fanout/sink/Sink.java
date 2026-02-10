package com.example.fanout.sink;

public interface Sink<T> {
  String name();
  void send(T payload) throws Exception;
}
