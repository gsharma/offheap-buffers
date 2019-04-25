package com.github.offheapbuffers;

/**
 * Off-heap ring-buffer implementation.
 * 
 * @author gaurav
 */
public class RingBufferImpl implements RingBuffer<Object> {
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
    // TODO
    writePointer = (writePointer + 1) % capacity;
    buffer[writePointer] = element;
    if (currentSize < buffer.length) {
      currentSize++;
    }
  }

  @Override
  public Object dequeue() {
    // TODO
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
