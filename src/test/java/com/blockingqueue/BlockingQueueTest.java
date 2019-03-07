package com.blockingqueue;

import com.blockingqueue.impl.MyBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertTrue;

public class BlockingQueueTest {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final int maxQueueItems = 1000;
  private final int messageCount = 200;
  private final int testThreads = 16;

  @Data
  @AllArgsConstructor
  public class TestData {
    final String threadName;
    final long iteration;
  }

  /**
   * Test reading and writing to Blocked queue.
   */
  @Test
  public void testMultiThreadingQueue() {
    // Blocking Queue to test
    final BlockingQueue<TestData> blockingQueue = new MyBlockingQueue<>(maxQueueItems);

    // Use this ThreadSafe Set to store queued objects, then remove them when they are dequeued to make sure all
    // objects were dequeued at the end.
    final Set<TestData> checkData = new CopyOnWriteArraySet<>();

    try {
      // Set up threads to read data from queue. Don't use completableFuture, since it's not completable.
      final List<Thread> readingThreads = new ArrayList<>();
      for (int i = 0; i < testThreads; i++) {
        Thread thread = new Thread(() -> {
          try {
            TestData testData;
            while ((testData = blockingQueue.get()) != null) {
              logger.info("getting: {}:{}", testData.getThreadName(), testData.getIteration());
              checkData.remove(testData);
            }
          } catch (InterruptedException e) {
            logger.info("interrupted normally");
          }
        });
        thread.start();
        readingThreads.add(thread);
      }

      // Set up threads to put data into queue
      final List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
      for (int i = 0; i < testThreads; i++) {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
          try {
            for (Integer j = 0; j < messageCount; j++) {
              TestData testData = new TestData(Thread.currentThread().getName(), j);
              checkData.add(testData);
              logger.info("putting: {}", testData.getIteration());
              blockingQueue.put(testData);
            }
          } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
          }
        });
        completableFutures.add(completableFuture);
      }

      // Fire them all of as simultaniously as possible. Seems to work better than a "get" after each runAsync().
      completableFutures.forEach( future -> {
        try {
          future.get();
        } catch (ExecutionException|InterruptedException e) {
          e.printStackTrace();
        }
      });

      // Done, so cancel the queue reader threads
      logger.info("Interrupt reading threads");
      readingThreads.forEach( thread -> {
        try {
          while (thread.getState() != Thread.State.WAITING) {
            Thread.sleep(100);
          }
          thread.interrupt();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });

      // Make sure we've accounted for all the queued items
      assertTrue("Didn't remove all queued items.", checkData.size()==0);
    } catch (Exception e) {
      assertTrue(e.getMessage(), false);
      e.printStackTrace();
    }
  }
}
