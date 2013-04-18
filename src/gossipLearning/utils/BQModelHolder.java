package gossipLearning.utils;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;

/**
 * The capacity of the container can be specified. This implementation uses 
 * array for container and works as a queue.
 * 
 * @author István Hegedűs
 */
public class BQModelHolder implements ModelHolder {
  private static final long serialVersionUID = 5887014943941802900L;
  
  /** The maximal capacity is Integer.MAX_VALUE by default. */
  public static final int MAX_CAPACITY = Integer.MAX_VALUE;
  /** The default capacity is 20. */
  public static final int DEFAULT_CAPACITY = 20;
  
  /** @hidden */
  private Model[] models;
  private final int bound;
  private int start;
  private int size;
  
  /**
   * Creates an object from this class with default capacity.
   */
  public BQModelHolder() {
    models = new Model[DEFAULT_CAPACITY];
    bound = DEFAULT_CAPACITY;
    start = 0;
    size = 0;
  }
  
  /**
   * Creates an object from this class with the specified capacity.
   * @param capacity - capacity of the container
   */
  public BQModelHolder(int capacity) {
    if (capacity > MAX_CAPACITY || capacity <= 0) {
      throw new RuntimeException("The capacity cannot be " + capacity + "!");
    }
    models = new Model[capacity];
    this.bound = capacity;
    start = 0;
    size = 0;
  }
  
  /**
   * Created an object from this class as a deep copy of the specified holder.
   * @param mh to be cloned
   */
  protected BQModelHolder(BQModelHolder mh) {
    this.bound = mh.bound;
    models = new Model[bound];
    for (int i = 0; i < mh.size(); i ++) {
      models[i] = (Model)mh.getModel(i).clone();
    }
    start = 0;
    size = mh.size;
  }
  
  @Override
  public Object clone(){
    return new BQModelHolder(this);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof BQModelHolder) {
      BQModelHolder h = (BQModelHolder)o;
      if (bound != h.bound) {
        return false;
      }
      if (size != h.size) {
        return false;
      }
      for (int i = 0; i < size; i++) {
        if (!models[(start + i) % bound].equals(h.models[(h.start + i) % h.bound])) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public int size() {
    return size;
  }

  @Override
  public Model getModel(int index) {
    if (index < 0 || index >= bound) {
      throw new IndexOutOfBoundsException("" + index);
    }
    if (size <= index) {
      return null;
    }
    return models[(start + index) % bound];
  }

  @Override
  public void setModel(int index, Model model) {
    if (index < 0 || index >= bound) {
      throw new IndexOutOfBoundsException("" + index);
    }
    if (size <= index) {
      return;
    }
    models[(start + index) % bound] = model;
  }

  /**
   * If the container if full, the first element will be removed.
   */
  @Override
  public void add(Model model) {
    models[(start + size) % bound] = model;
    if (size < bound) {
      size ++;
    } else {
      start = (start + 1) % bound;
    }
  }

  @Override
  public Model remove(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("" + index);
    }
    Model ret = models[(start + index) % bound];
    for (int i = index; i < size; i++) {
      models[(start + i) % bound] = models[(start + i + 1) % bound];
    }
    models[(start + size - 1) % bound] = null;
    size --;
    return ret;
  }
  
  @Override
  public Model removeFirst() {
    Model ret = null;
    if (size > 0) {
      ret = models[start];
      models[start] = null;
      start = (start + 1) % bound;
      size --;
    }
    return ret;
  }

  @Override
  public void clear() {
    for (int i = 0; i < models.length; i++) {
      models[i] = null;
    }
    size = 0;
  }
  
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append(size);
    b.append('\t');
    b.append(bound);
    b.append('\t');
    b.append('[');
    for (int i = start; i < start + size; i++) {
      if (i == start) {
        b.append(models[i % bound]);
      } else {
        b.append(", " + models[i % bound]);
      }
    }
    b.append(']');
    return b.toString();
  }

}
