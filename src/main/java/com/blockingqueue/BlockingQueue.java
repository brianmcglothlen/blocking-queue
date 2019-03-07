package com.blockingqueue;

public interface BlockingQueue<T> {
  void put(T obj) throws InterruptedException;
  T get() throws InterruptedException;
}
