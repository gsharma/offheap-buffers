package com.github.offheapbuffers;

/**
 * Ring Buffer skeleton.
 * 
 * @author gaurav
 */
public interface RingBuffer<T> {

  /**
   * Mode of operation of this buffer
   */
  RingBufferMode getMode();

  /**
   * Enqueue an element to this buffer
   */
  void enqueue(T element) throws RingBufferException;

  /**
   * Dequeue an element from this buffer. Note that as opposed to peek(), dequeue() removes the
   * element.
   */
  T dequeue() throws RingBufferException;

  /**
   * Peek without dequeueing from the buffer
   */
  T peek() throws RingBufferException;

  /**
   * Get the current number of elements in this buffer
   */
  int currentSize();

  /**
   * Get the capacity of this buffer. Note that for post-construction, this is immutable.
   */
  int capacity();

  /**
   * Delete elements if any, in the buffer.
   */
  void clear() throws RingBufferException;

  /**
   * A simple builder to let users use fluent APIs to build RingBuffer.
   */
  public final static class RingBufferBuilder<T> {
    private RingBufferMode mode;
    private int capacity;

    public static RingBufferBuilder<?> newBuilder() {
      return new RingBufferBuilder<>();
    }

    public RingBufferBuilder<T> mode(final RingBufferMode mode) {
      this.mode = mode;
      return this;
    }

    public RingBufferBuilder<T> capacity(final int capacity) {
      this.capacity = capacity;
      return this;
    }

    public RingBuffer<T> build() throws RingBufferException {
      return new HeapRingBuffer<T>(mode, capacity);
    }

    private RingBufferBuilder() {}
  }

}
