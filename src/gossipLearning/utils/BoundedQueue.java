package gossipLearning.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class represents a bounded queue that can store objects from the specified 
 * class. This class have to be Serializable. This bound cannot be changed after the 
 * construction. This queue has array implementation and need constant time for add, 
 * get and remove methods. The index based remove needs linear time and not 
 * recommended.<br/>
 * @author István Hegedűs
 *
 * @param <T> Type of object to be stored.
 */
public class BoundedQueue<T extends Serializable > implements Serializable {
  private static final long serialVersionUID = -4454582039733365477L;
  
  private T[] queue;
  private final int bound;
  private int size;
  private int startPosition;
  
  /**
   * Constructs bounded queue with the specified capacity.
   * @param bound the maximal capacity of the queue.
   */
  @SuppressWarnings("unchecked")
  public BoundedQueue(int bound) {
    this.bound = bound;
    queue = (T[])new Serializable[bound];
    size = 0;
    startPosition = 0;
  }
  
  /**
   * Makes a deep copy of this object.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object clone() {
    BoundedQueue<T> ret = new BoundedQueue<T>(bound);
    ret.size = size;
    ret.startPosition = startPosition;
    System.arraycopy(queue, 0, ret.queue, 0, queue.length);
    for (int i = 0; i < size; i++) {
      try {
        ret.queue[(i + startPosition) % bound] = (T)queue[(i + startPosition) % bound].getClass().getMethod("clone").invoke(queue[(i + startPosition) % bound]);
      } catch (NoSuchMethodException e) {
        try {
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          ObjectOutputStream out = new ObjectOutputStream(byteOut);
          out.writeObject(queue[(i + startPosition) % bound]);
          ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
          ret.queue[(i + startPosition) % bound] = (T)in.readObject();
          in.close();
          out.close();
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return ret;
  }
  
  /**
   * Adds the specified element to the end of the queue. Returns and removes the 
   * first element of the queue if it is full, or null otherwise. 
   * @param e value to add
   * @return first element if the queue is full, null otherwise
   */
  public T add(T e) {
    T ret;
    int index = (startPosition + size) % bound;
    ret = queue[index];
    queue[index] = e;
    if (size < bound) {
      size++;
      ret = null;
    } else {
      startPosition = (startPosition + 1) % bound;
    }
    return ret;
  }
  
  /**
   * Returns the element of the queue at the specified index.
   * @param index the index of the element to return
   * @return element of the queue at index
   */
  public T get(int index) {
    if (index >= size || index < 0) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    return queue[(startPosition + index) % bound];
  }
  
  /**
   * Returns and removes the first element of the queue.
   * @return the first element of the queue
   */
  public T remove() {
    T ret = null;
    if (size > 0) {
      ret = queue[startPosition];
      size--;
      startPosition = (startPosition + 1) % bound;
    }
    return ret;
  }
  
  /**
   * Returns and removes the element of the queue at the specified index. <br/>
   * NOTE: This function has linear time, and not recommended.
   * @param index index of the element to remove
   * @return removed element
   */
  public T remove(int index) {
    T ret = null;
    if (index >= size || index < 0) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    ret = get(index);
    int idxTo, idxFrom;
    if (index < size / 2) {
      for (int i = 0; i < index; i++) {
        idxTo = (startPosition + i + 1) % bound;
        idxFrom = (startPosition + i) % bound;
        queue[idxTo] = queue[idxFrom];
      }
      startPosition = (startPosition + 1) % bound;
    } else {
      for (int i = index; i < size; i++) {
        idxTo = (startPosition + i) % bound;
        idxFrom = (startPosition + i + 1) % bound;
        queue[idxTo] = queue[idxFrom];
      }
    }
    size--;
    return ret;
  }
  
  /**
   * Returns the size of the queue, the number of stored elements.
   * @return size of the queue
   */
  public int size() {
    return size;
  }
  
  /**
   * Returns the capacity of the queue.
   * @return capacity of the queue
   */
  public int getBound() {
    return bound;
  }
  
  /**
   * Resets the queue.
   */
  public void reset() {
    startPosition = 0;
    size = 0;
  }
  
  /**
   * Returns the String representation of the object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < size; i++) {
      if (i == 0) {
        sb.append(queue[(startPosition + i) % bound].toString());
      } else {
        sb.append(" " + queue[(startPosition + i) % bound].toString());
      }
    }
    return sb.toString();
  }
  
  /**
   * Just for testing.
   * @param args command line arguments.
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    BoundedQueue<Integer> bq = new BoundedQueue<Integer>(5);
    for (int i = 0; i < 10; i++) {
      Integer ret = bq.add(i);
      System.out.println("ADD(" + i + "):" + ret + "\t" + bq + "\t" + bq.size());
    }
    int size = bq.size();
    BoundedQueue<Integer> bqClone = (BoundedQueue<Integer>)bq.clone();
    System.out.println(bqClone);
    for (int i = 0; i < size; i++) {
      System.out.println("REMOVE:" + bq.remove() + "\t" + bq + "\t" + bq.size());
    }
    System.out.println("REMOVE:" + bq.remove() + "\t" + bq + "\t" + bq.size());
  }
  
}
