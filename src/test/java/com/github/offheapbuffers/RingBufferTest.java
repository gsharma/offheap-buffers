package com.github.offheapbuffers;

import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.github.offheapbuffers.RingBuffer.RingBufferBuilder;

/**
 * Tests for maintaining sanctity of the RingBuffer.
 * 
 * @author gaurav
 */
public final class RingBufferTest {
  private static final Logger logger = LogManager.getLogger(RingBufferTest.class.getSimpleName());

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

  @Test
  public void testSPSCBufferPeek() throws Exception {
    final RingBuffer<Long> buffer = new HeapRingBuffer<Long>(RingBufferMode.OVERWRITE, 10);
    assertEquals(0, buffer.currentSize());
    final int chunk = 50;
    final Thread filler = new Thread("filler") {
      @Override
      public void run() {
        while (!interrupted()) {
          try {
            for (int iter = 0; iter < chunk; iter++) {
              buffer.enqueue(System.currentTimeMillis());
            }
            logger.info("Filled {}", chunk);
          } catch (RingBufferException problem) {
            logger.error(problem);
          }
        }
      }
    };
    final Thread peeker = new Thread("peeker") {
      @Override
      public void run() {
        while (!interrupted()) {
          try {
            int peeked = 0;
            for (int iter = 0; iter < chunk; iter++) {
              if (buffer.peek() != null) {
                peeked++;
              }
            }
            logger.info("Peeked {}", peeked);
          } catch (RingBufferException problem) {
            logger.error(problem);
          }
        }
      }
    };
    filler.start();
    peeker.start();
    Thread.sleep(10L);
    filler.interrupt();
    peeker.interrupt();
    filler.join();
    peeker.join();
  }

  @Test
  public void testHeapBufferBlocking() throws RingBufferException {
    int size = 5;
    final RingBuffer<String> buffer = new HeapRingBuffer<String>(RingBufferMode.BLOCK, size);
    assertEquals(0, buffer.currentSize());

    // 1. load buffer
    buffer.enqueue("1");
    buffer.enqueue("2");
    buffer.enqueue("3");
    buffer.enqueue("4");
    buffer.enqueue("5"); // writePointer to end
    assertEquals(buffer.capacity(), buffer.currentSize());

    // 2. unload and fill buffer as room becomes available
    assertEquals("1", buffer.dequeue()); // readPointer at start
    assertEquals(size - 1, buffer.currentSize());
    buffer.enqueue("6");
    assertEquals(buffer.capacity(), buffer.currentSize());
    assertEquals("2", buffer.dequeue()); // keep moving readPointer
    assertEquals(size - 1, buffer.currentSize());
    buffer.enqueue("7"); // writePointer trails readPointer
    assertEquals(buffer.capacity(), buffer.currentSize());
    assertEquals("3", buffer.peek());

    // 3. BLOCK mode shouldn't allow writePointer to clobber unread data
    try {
      buffer.enqueue("8");
    } catch (RingBufferException problem) {
      assertEquals(RingBufferException.Code.BLOCK_MODE_CLOBBER_ATTEMPT, problem.getCode());
    }
    // logger.info(buffer);
  }

}
