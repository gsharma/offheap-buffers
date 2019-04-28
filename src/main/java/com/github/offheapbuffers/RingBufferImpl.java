package com.github.offheapbuffers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * On-heap fixed-size simple ring-buffer implementation.
 * 
 * @author gaurav
 */
public class RingBufferImpl implements RingBuffer<Object> {
  private static final Logger logger = LogManager.getLogger(RingBuffer.class.getSimpleName());
  private final Storage storage;
  private final int capacity;
  private int currentSize;

  // possibly have multiple read pointers, 1 each for a reader
  private int readPointer;

  // assume a single writer
  private int writePointer;

  private final Object[] buffer;

  public RingBufferImpl(final Storage storage, final int capacity) {
    this.storage = storage;
    this.capacity = capacity;
    buffer = new Object[capacity];
  }

  @Override
  public void enqueue(final Object element) {
    writePointer = (writePointer + 1) % capacity;
    buffer[writePointer] = element;
    if (currentSize < buffer.length) {
      currentSize++;
    }
  }

  @Override
  public Object dequeue() {
    Object dequeued = null;
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
    return dequeued;
  }

  @Override
  public Object peek() {
    Object peeked = null;
    int nextReadCandidate = (readPointer + 1) % capacity;
    if (nextReadCandidate < buffer.length) {
      if (buffer[nextReadCandidate] != null) {
        peeked = buffer[nextReadCandidate];
      }
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

}

