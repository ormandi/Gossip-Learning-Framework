package tests.gossipLearning.utils;

import gossipLearning.utils.BoundedQueue;

import java.io.Serializable;

import junit.framework.TestCase;

public class BoundedQueueTest extends TestCase implements Serializable {
  private static final long serialVersionUID = -5320129841584587519L;
  
  private static final int bound = 5;
  @SuppressWarnings("unchecked")
  public void testClone() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    BoundedQueue<Integer> bqc;
    for (int i = 0; i < 12; i++) {
      bq.add(i);
    }
    bqc = (BoundedQueue<Integer>)bq.clone();
    assertEquals(bq.toString(), bqc.toString());
    assertEquals(bq.size(), bqc.size());
    assertEquals(bq.getBound(), bqc.getBound());
    bqc.remove();
    assertFalse(bq.toString().equals(bqc.toString()));
  }
  
  public void testAdd() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    assertEquals("", bq.toString());
    bq.add(0);
    assertEquals("0", bq.toString());
    bq.add(1);
    assertEquals("0 1", bq.toString());
    bq.add(2);
    assertEquals("0 1 2", bq.toString());
    bq.add(3);
    assertEquals("0 1 2 3", bq.toString());
    bq.add(4);
    assertEquals("0 1 2 3 4", bq.toString());
    bq.add(5);
    assertEquals("1 2 3 4 5", bq.toString());
    bq.add(6);
    assertEquals("2 3 4 5 6", bq.toString());
  }
  
  public void testGet() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    for (int i = 0; i < 7; i++) {
      bq.add(i);
    }
    assertEquals(2, bq.get(0).intValue());
    assertEquals(4, bq.get(2).intValue());
  }
  
  public void testRemove() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    assertEquals(null, bq.remove());
    for (int i = 0; i < 7; i++) {
      bq.add(i);
    }
    assertEquals(2, bq.remove().intValue());
    assertEquals("3 4 5 6", bq.toString());
  }
  
  public void testRemoveByIndex() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    for (int i = 0; i < 7; i++) {
      bq.add(i);
    }
    assertEquals(5, bq.remove(3).intValue());
    assertEquals("2 3 4 6", bq.toString());
  }
  
  public void testSize() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    assertEquals(0, bq.size());
    for (int i = 0; i < 7; i++) {
      if (i < bound) {
        assertEquals(i, bq.size());
      } else {
        assertEquals(bound, bq.size());
      }
      bq.add(i);
    }
  }
  
  public void testBound() {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(bound);
    assertEquals(bound, bq.getBound());
  }
}
