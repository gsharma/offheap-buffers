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
   * Dequeue an element from this buffer
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
   * Determine if the buffer is full.
   */
  // boolean isFull();

  /**
   * Determine if the buffer is empty.
   */
  // boolean isEmpty();

  /**
   * Delete elements if any, in the buffer.
   */
  void clear() throws RingBufferException;

}
