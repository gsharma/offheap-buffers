package com.github.offheapbuffers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for maintaining sanctity of the RingBuffer.
 * 
 * @author gaurav
 */
public final class RingBufferTest {

  @Test
  public void testHeapBufferPeek() throws RingBufferException {
    int size = 5;
    final RingBuffer<String> buffer = new HeapRingBuffer<String>(RingBufferMode.OVERWRITE, size);
    assertEquals(0, buffer.currentSize());

    // 1. load buffer
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5"); // writePointer to end
    assertEquals(buffer.capacity(), buffer.currentSize());
    assertEquals(size, buffer.currentSize());

    // 2. peek and unload buffer
    assertEquals("1", buffer.peek()); // readPointer at start
    assertEquals("1", buffer.peek());
    assertEquals(size, buffer.currentSize());
    assertEquals("1", buffer.dequeue());
    assertEquals(size - 1, buffer.currentSize());
    assertEquals("2", buffer.peek());
    assertEquals(size - 1, buffer.currentSize());
    assertEquals("2", buffer.dequeue());
    assertEquals("3", buffer.dequeue());
    assertEquals("4", buffer.dequeue());
    assertEquals("5", buffer.dequeue());
    assertEquals(0, buffer.currentSize());

    assertNull(buffer.peek());
    assertNull(buffer.dequeue());
  }

  @Test
  public void testHeapBufferDrain() throws RingBufferException {
    int size = 5;
    final RingBuffer<String> buffer = new HeapRingBuffer<String>(RingBufferMode.OVERWRITE, size);
    assertEquals(0, buffer.currentSize());

    // 1. load buffer
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5"); // writePointer to end
    assertEquals(buffer.capacity(), buffer.currentSize());

    // 2. unload buffer
    assertEquals("1", buffer.dequeue()); // readPointer at start
    assertEquals(size - 1, buffer.currentSize());
    assertEquals("2", buffer.dequeue());
    assertEquals(size - 2, buffer.currentSize());
    assertEquals("3", buffer.dequeue());
    assertEquals(size - 3, buffer.currentSize());
    assertEquals("4", buffer.dequeue());
    assertEquals(size - 4, buffer.currentSize());
    assertEquals("5", buffer.dequeue());
    assertEquals(size - 5, buffer.currentSize());
    assertEquals(0, buffer.currentSize());
    assertNull(buffer.dequeue());

    // 3. rinse and repeat of 1.
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5");
    assertEquals(buffer.capacity(), buffer.currentSize());

    // 4. rinse and repeat of 2.
    assertEquals("1", buffer.dequeue());
    assertEquals("2", buffer.dequeue());
    assertEquals("3", buffer.dequeue());
    assertEquals("4", buffer.dequeue());
    assertEquals("5", buffer.dequeue());
    assertEquals(0, buffer.currentSize());

    // 5. wrap-around
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5");
    buffer.enqueue("6");
    buffer.enqueue("7");
    buffer.enqueue("8");
    buffer.enqueue("9");
    buffer.enqueue("10");
    assertEquals(buffer.capacity(), buffer.currentSize());

    // 6. unload buffer
    assertEquals("6", buffer.dequeue()); // readPointer at start
    assertEquals("7", buffer.dequeue());
    assertEquals("8", buffer.dequeue());
    assertEquals("9", buffer.dequeue());
    assertEquals("10", buffer.dequeue());
    assertEquals(0, buffer.currentSize());
    assertNull(buffer.dequeue());
  }

  @Test
  public void testHeapBufferNoOverflow() throws RingBufferException {
    final RingBuffer<Integer> buffer = new HeapRingBuffer<Integer>(RingBufferMode.OVERWRITE, 9);
    assertEquals(0, buffer.currentSize());
    for (int iter = 0; iter < 100; iter++) {
      buffer.enqueue(iter);
    }
    assertEquals(buffer.capacity(), buffer.currentSize());
  }

}
