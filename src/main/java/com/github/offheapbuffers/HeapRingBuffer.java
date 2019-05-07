package com.github.offheapbuffers;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.offheapbuffers.RingBufferException.Code;

/**
 * On-heap fixed-size simple ring-buffer implementation that's best used as a single producer single
 * consumer (spsc) ring-buffer.
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

  public HeapRingBuffer(/* final Storage storage, */ final int capacity)
      throws RingBufferException {
    if (capacity <= 0) {
      throw new RingBufferException(Code.INVALID_BUFFER_SIZE);
    }
    // this.storage = storage;
    this.capacity = capacity;
    buffer = new Object[capacity];
  }

  @Override
  public void enqueue(final Object element) throws RingBufferException {
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
      throw new RingBufferException(Code.ENQUEUE_LOCK_FAILED);
    }
  }

  @Override
  public Object dequeue() throws RingBufferException {
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
      throw new RingBufferException(Code.DEQUEUE_LOCK_FAILED);
    }
    return dequeued;
  }

  @Override
  public Object peek() throws RingBufferException {
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
      throw new RingBufferException(Code.PEEK_LOCK_FAILED);
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

  @Override
  public void clear() throws RingBufferException {
    if (writeLock.tryLock()) {
      try {
        for (int iter = 0; iter < buffer.length; iter++) {
          buffer[iter] = null;
        }
        currentSize = 0;
      } finally {
        writeLock.unlock();
      }
    } else {
      logger.error("Failed to acquire lock to clear the buffer");
      throw new RingBufferException(Code.CLEAR_LOCK_FAILED);
    }
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("HeapRingBuffer:");
    builder.append(Arrays.deepToString(buffer));
    return builder.toString();
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

}

