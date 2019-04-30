package com.github.offheapbuffers;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * On-heap fixed-size simple ring-buffer implementation.
 * 
 * @author gaurav
 */
public final class HeapRingBuffer implements RingBuffer<Object> {
  private static final Logger logger = LogManager.getLogger(RingBuffer.class.getSimpleName());

  private final ReentrantReadWriteLock superLock = new ReentrantReadWriteLock(true);
  private final WriteLock writeLock = superLock.writeLock();
  private final ReadLock readLock = superLock.readLock();

  // private final Storage storage;
  private final int capacity;
  private volatile int currentSize;

  // assume a single read pointer
  private int readPointer;

  // assume a single write pointer
  private int writePointer;

  private final Object[] buffer;

  public HeapRingBuffer(/* final Storage storage, */ final int capacity) {
    // this.storage = storage;
    this.capacity = capacity;
    buffer = new Object[capacity];
  }

  @Override
  public void enqueue(final Object element) {
    if (writeLock.tryLock()) {
      try {
        writePointer = (writePointer + 1) % capacity;
        buffer[writePointer] = element;
        if (currentSize < buffer.length) {
          currentSize++;
        }
      } finally {
        writeLock.unlock();
      }
    } else {
      logger.error("Failed to acquire lock to enqueue {}", element);
    }
  }

  @Override
  public Object dequeue() {
    Object dequeued = null;
    if (writeLock.tryLock()) {
      try {
        int nextReadCandidate = (readPointer + 1) % capacity;
        if (nextReadCandidate < buffer.length) {
          if (buffer[nextReadCandidate] != null) {
            readPointer = nextReadCandidate;
            dequeued = buffer[readPointer];
            buffer[readPointer] = null;
            if (currentSize > 0) {
              currentSize--;
            }
          }
        }
      } finally {
        writeLock.unlock();
      }
    } else {
      logger.error("Failed to acquire lock to dequeue from buffer");
    }
    return dequeued;
  }

  @Override
  public Object peek() {
    Object peeked = null;
    if (readLock.tryLock()) {
      try {
        int nextReadCandidate = (readPointer + 1) % capacity;
        if (nextReadCandidate < buffer.length) {
          if (buffer[nextReadCandidate] != null) {
            peeked = buffer[nextReadCandidate];
          }
        }
      } finally {
        readLock.unlock();
      }
    } else {
      logger.error("Failed to acquire lock to peek into buffer");
    }
    return peeked;
  }

  @Override
  public int currentSize() {
    return currentSize;
  }

  @Override
  public int capacity() {
    return capacity;
  }

  // unnecessary and can be deduced from currentSize, capacity
  @Override
  public boolean isFull() {
    return currentSize() == capacity();
  }

  // unnecessary and can be deduced from currentSize
  @Override
  public boolean isEmpty() {
    return currentSize() == 0;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("HeapRingBuffer:");
    builder.append(Arrays.deepToString(buffer));
    return builder.toString();
  }

}

