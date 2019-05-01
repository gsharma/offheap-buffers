package com.github.offheapbuffers;

/**
 * Ring Buffer skeleton.
 * 
 * @author gaurav
 */
public interface RingBuffer<T> {

  /**
   * Enqueue an element to this buffer
   */
  void enqueue(T element);

  /**
   * Dequeue an element from this buffer
   */
  T dequeue();

  /**
   * Peek without dequeueing from the buffer
   */
  T peek();

  /**
   * Get the current number of elements in this buffer
   */
  int currentSize();

  /**
   * Get the capacity of this buffer. Note that for post-construction, this is immutable.
   */
  int capacity();

  /**
   * Determine if the buffer is full.
   */
  boolean isFull();

  /**
   * Determine if the buffer is empty.
   */
  boolean isEmpty();

  /**
   * Delete elements if any, in the buffer.
   */
  void clear();

}
