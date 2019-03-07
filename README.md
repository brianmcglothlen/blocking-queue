# Blocking Queue
Implement a blocking queue where the queue has a limited size and the put blocks if the queue is full, and the get blocks if the queue is empty.

# Purpose
Demonstrate how to use wait() and notifyAll() to protect a shared resource that isn't protected in a multi-threaded environment. What makes this a bit challenging is the requirement to create a blocking queue that has a limited number of positions available in the queue. This means simply using a semaphore to protect the queue is impossible, because once the you are inside the semaphore blocked waiting for the queue, the other semaphore is also blocked. This would create a deadlock.

This is demonstration that just implements a blocking queue, since there already is a blocking queue implementation in Java found here: https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/LinkedBlockingQueue.html.

# API
```
public interface BlockingQueue<T> {
  void put(T obj) throws InterruptedException;
  T get() throws InterruptedException;
}
```

# Code
```
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
```

# Usage  
```
final BlockingQueue<TestData> blockingQueue = new MyBlockingQueue<>(maxQueueItems);
...  
thread1.put(<someObject>);
...
SomeObject someobject = thread2.get();  
```

# Tests
Uses JUnit to test Blocking Queue by creating many ""putter" and "getter" threads. It uses both "Thread" and "CompletableFuture". CompletableFuture is only used on the putter end because the getters never "Complete", and so I thought it was bad form to cancel them while they were waiting. 

