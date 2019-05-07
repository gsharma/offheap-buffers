package com.github.offheapbuffers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    final RingBuffer buffer = new HeapRingBuffer(size);
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());

    // 1. load buffer
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5"); // writePointer to end
    assertFalse(buffer.isEmpty());
    assertTrue(buffer.isFull());
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
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());
    assertNull(buffer.peek());
    assertNull(buffer.dequeue());
  }

  @Test
  public void testHeapBufferDrain() throws RingBufferException {
    int size = 5;
    final RingBuffer buffer = new HeapRingBuffer(size);
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());

    // 1. load buffer
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5"); // writePointer to end
    assertFalse(buffer.isEmpty());
    assertTrue(buffer.isFull());
    assertEquals(size, buffer.currentSize());

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
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());
    assertNull(buffer.dequeue());

    // 3. rinse and repeat of 1.
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5");
    assertFalse(buffer.isEmpty());
    assertTrue(buffer.isFull());

    // 4. rinse and repeat of 2.
    assertEquals("1", buffer.dequeue());
    assertEquals("2", buffer.dequeue());
    assertEquals("3", buffer.dequeue());
    assertEquals("4", buffer.dequeue());
    assertEquals("5", buffer.dequeue());
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());

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
    assertFalse(buffer.isEmpty());
    assertTrue(buffer.isFull());
    assertEquals(size, buffer.currentSize());

    // 6. unload buffer
    assertEquals("6", buffer.dequeue()); // readPointer at start
    assertEquals("7", buffer.dequeue());
    assertEquals("8", buffer.dequeue());
    assertEquals("9", buffer.dequeue());
    assertEquals("10", buffer.dequeue());
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());
    assertNull(buffer.dequeue());
  }

  @Test
  public void testHeapBufferNoOverflow() throws RingBufferException {
    final RingBuffer buffer = new HeapRingBuffer(9);
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());
    for (int iter = 0; iter < 100; iter++) {
      buffer.enqueue(iter);
    }
    assertFalse(buffer.isEmpty());
    assertTrue(buffer.isFull());
  }

}
