package com.github.offheapbuffers;

/**
 * Ring Buffer skeleton.
 * 
 * @author gaurav
 */
public interface RingBuffer {
  void enqueue(Object element);

  Object dequeue();

  int currentSize();

  boolean isFull();

  boolean isEmpty();

}
