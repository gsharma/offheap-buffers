package com.github.offheapbuffers;

/**
 * Off-heap ring-buffer implementation.
 * 
 * @author gaurav
 */
public class RingBufferImpl implements RingBuffer<Object> {
  private final Storage storage;
  private final int capacity;

  // possibly have multiple read pointers, 1 each for a reader
  private int readPointer;

  // assume a single writer
  private int writePointer;

  public RingBufferImpl(final Storage storage, final int capacity) {
    this.storage = storage;
    this.capacity = capacity;
  }

  @Override
  public void enqueue(Object element) {
    // TODO
  }

  @Override
  public Object dequeue() {
    // TODO
    return null;
  }

  @Override
  public int currentSize() {
    // TODO
    return 0;
  }

  @Override
  public int capacity() {
    return capacity;
  }

  @Override
  public boolean isFull() {
    // TODO
    return false;
  }

  @Override
  public boolean isEmpty() {
    // TODO
    return false;
  }

}

