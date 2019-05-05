package com.github.offheapbuffers;

/**
 * Off-heap fixed-size simple ring-buffer implementation.
 * 
 * @author gaurav
 */
public final class OffheapRingBuffer implements RingBuffer<Object> {

  @Override
  public void enqueue(Object element) throws RingBufferException {
    // TODO
  }

  @Override
  public Object dequeue() throws RingBufferException {
    // TODO
    return null;
  }

  @Override
  public Object peek() throws RingBufferException {
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
    // TODO
    return 0;
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

  @Override
  public void clear() throws RingBufferException {
    // TODO
  }

}
