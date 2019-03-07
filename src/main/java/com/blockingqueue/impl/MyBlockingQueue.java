package com.blockingqueue.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Implement BlockingQueue before arrival of Java 1.5: java.util.concurrent.LinkedBlockingQueue(capacity)
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/LinkedBlockingQueue.html
 *
 * @param <T> Any object type to queue
 */
public class MyBlockingQueue<T> implements com.blockingqueue.BlockingQueue<T> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final List<T> queue = new LinkedList<>();
  private final int limit;

  public MyBlockingQueue(int limit){
    this.limit = limit;
  }

  @Override
  public synchronized void put(T item) throws InterruptedException  {
    while (queue.size() == limit) {
      // Wait
      logger.info("waiting");
      wait();
    }

    if(queue.size() == 0) {
      notifyAll();
    }

    queue.add(item);
  }

  @Override
  public synchronized T get() throws InterruptedException {
    while (queue.size() == 0){
      wait();
    }

    if(queue.size() == limit) {
      logger.info("notifying");
      notifyAll();
    }

    return queue.remove(0);
  }
}
