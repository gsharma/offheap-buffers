package com.github.offheapbuffers;

/**
 * Unified single exception that's thrown and handled by this RingBuffer. The idea is to use the
 * code enum to encapsulate various error/exception conditions. That said, stack traces, where
 * available and desired, are not meant to be kept from users.
 * 
 * @author gaurav
 */
public final class RingBufferException extends Exception {
  private static final long serialVersionUID = 1L;

  private final Code code;

  public RingBufferException(final Code code) {
    super(code.getDescription());
    this.code = code;
  }

  public RingBufferException(final Code code, final String message) {
    super(message);
    this.code = code;
  }

  public RingBufferException(final Code code, final Throwable throwable) {
    super(throwable);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }

  public static enum Code {
    // 0.
    INVALID_BUFFER_SIZE("Ring buffer size is less than or equal to zero"),
    // 1.
    ENQUEUE_LOCK_FAILED("Failed to acquire write lock for enqueueing to ring buffer"),
    // 2.
    DEQUEUE_LOCK_FAILED("Failed to acquire write lock for dequeueing from ring buffer"),
    // 3.
    PEEK_LOCK_FAILED("Failed to acquire read lock for peeking into ring buffer"),
    // 4.
    CLEAR_LOCK_FAILED("Failed to acquire write lock for clearing ring buffer"),
    // 5.
    BLOCK_MODE_CLOBBER_ATTEMPT(
        "Attempted to enqueue and clobber unread ring buffer data in BLOCK mode"),
    // n.
    UNKNOWN_FAILURE(
        "Ring buffer failed. Check exception stacktrace for more details of the failure");

    private String description;

    private Code(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

}
