package gossipLearning.utils;

import gossipLearning.interfaces.VectorEntry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements a dense vector by double array. <br/>
 * For get, put and remove methods uses O(1) time. The iteration is 
 * linear time and enumerates the indices in ascendant order. <br/>
 * The default size of this container is 16. If the container is full, it grows 
 * automatically by a factor, that is 1.5 by default.
 * @author István Hegedűs
 *
 */
public class DenseVector implements Serializable, Iterable<VectorEntry>, Comparable<DenseVector> {
  private static final long serialVersionUID = -7977731819159536718L;
  private static final int defaultCapacity = 16;
  private static final double defaultGrowFactor = 1.5;

  private final double growFactor;

  private double[] values;
  private int index;
  
  /**
   * Constructs a DenseVector instance with capacity 16, growing factor 1.5.
   */
  public DenseVector() {
    this(defaultCapacity, defaultGrowFactor);
  }
  
  /**
   * Constructs a DenseVector instance with the specified capacity and 
   * default grow factor.
   * @param capacity capacity of the vector
   */
  public DenseVector(int capacity) {
    this(capacity, defaultGrowFactor);
  }
  
  /**
   * Constructs a SparseVector instance with the specified capacity and 
   * the specified grow factor.
   * @param capacity capacity of the vector
   * @param growFactor the factor of the growth
   */
  public DenseVector(int capacity, double growFactor) {
    this.growFactor = growFactor;
    values = new double[capacity];
    Arrays.fill(values, 0.0);
    index = Integer.MIN_VALUE;
  }
  
  /**
   * Constructs a DenseVector by makes a deep copy of the specified vector.
   * @param vector vector to be cloned
   */
  public DenseVector(DenseVector vector) {
    this(vector.values.length, vector.growFactor);
    System.arraycopy(vector.values, 0, values, 0, vector.values.length);
    index = vector.index;
  }
  
  /**
   * Constructs a DenseVector from the specified double array vector.
   * @param vector array to be stored
   */
  public DenseVector(double[] vector) {
    this(vector.length);
    for (int i = 0; i < vector.length; i++) {
      values[i] = vector[i];
    }
  }
  
  /**
   * Constructs a DenseVector from the specified Map<Integer, Double> vector.
   * @param vector array to be stored
   */
  public DenseVector(Map<Integer, Double> vector) {
    this(vector.size());
    for (Map.Entry<Integer, Double> e : vector.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }
  
  /**
   * Constructs a DenseVector from the specified SparseVector vector.
   * @param vector vector to be stored
   */
  public DenseVector(SparseVector vector) {
    this(vector.maxIndex() + 1);
    for (VectorEntry e : vector) {
      values[e.index] = e.value;
    }
  }
  
  /**
   * Makes a deep copy of the current vector.
   */
  public Object clone() {
    return new DenseVector(this);
  }
  
  /**
   * Two dense vectors are equal if have the same values at the same positions.
   */
  public boolean equals(Object vector) {
    if (!(vector instanceof DenseVector)) {
      return false;
    }
    for (int i = 0; i < Math.max(values.length, ((DenseVector)vector).values.length); i++) {
      if (i < values.length && i < ((DenseVector)vector).values.length) {
        if (values[i] != ((DenseVector)vector).values[i]) {
          return false;
        }
      } else if (i < values.length) {
        if (values[i] != 0.0) {
          return false;
        }
      } else {
        if (((DenseVector)vector).values[i] != 0.0) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public Iterator<VectorEntry> iterator() {
    index = -1;
    return new DenseVectorIterator();
  }

  /**
   * Returns the value that is stored at the specified index.
   * @param index index of the value to be returned
   * @return value at the specified index
   */
  public double get(int index) {
    return values[index];
  }
  
  /**
   * Resizes the vector by the factor of growth.
   */
  private void grow(int minSize) {
    int capacity = Math.max((int)(values.length * growFactor), minSize + 1);
    double[] newValues = new double[capacity];
    for (int i = 0; i < capacity; i++) {
      if (i < values.length) {
        newValues[i] = values[i];
      } else {
        newValues[i] = 0.0;
      }
    }
    values = newValues;
  }

  /**
   * Stores the specified value at the specified index. The value, that is stored, at 
   * the specified index will be overridden.
   * @param index index to store at
   * @param value value to be stored
   */
  public void put(int index, double value) {
    if (index >= values.length - 1) {
      grow(index);
    }
    values[index] = value;
  }

  /**
   * Removes and returns the value at the specified index.
   * @param index index of value to be removed
   * @return the value that was removed
   */
  public double remove(int index) {
    double ret = values[index];
    values[index] = 0.0;
    return ret;
  }

  /**
   * Adds the specified DenseVector to the vector that is represented by the current object. 
   * @param vector to be added
   * @return the sum of the specified vector and this
   */
  public DenseVector add(DenseVector vector) {
    return add(vector, 1.0);
  }
  
  /**
   * Adds the specified SparseVector to the vector that is represented by the current object. 
   * @param vector to be added
   * @return the sum of the specified vector and this
   */
  public DenseVector add(SparseVector vector) {
    return add(vector, 1.0);
  }

  /**
   * Adds the specified DenseVector to the vector, that is represented by the current 
   * object, by the specified alpha times.
   * @param vector to be added
   * @param alpha scale factor of the addition
   * @return the sum of this and the alpha times of the vector
   */
  public DenseVector add(DenseVector vector, double alpha) {
    if (alpha == 0.0) {
      return this;
    }
    if (vector.values.length > values.length) {
      grow(vector.values.length);
    }
    for (int i = 0; i < vector.values.length; i++) {
      values[i] += vector.values[i] * alpha;
    }
    return this;
  }
  
  /**
   * Adds the specified SparseVector to the vector, that is represented by the current 
   * object, by the specified alpha times.
   * @param vector to be added
   * @param alpha scale factor of the addition
   * @return the sum of this and the alpha times of the vector
   */
  public DenseVector add(SparseVector vector, double alpha) {
    if (alpha == 0.0) {
      return this;
    }
    if (vector.maxIndex() > values.length -1) {
      grow(vector.maxIndex() + 1);
    }
    for (VectorEntry e : vector) {
      values[e.index] += e.value * alpha;
    }
    return this;
  }

  /**
   * Scales the current vector by the specified value.
   * @param alpha the scale factor
   * @return this
   */
  public DenseVector mul(double alpha) {
    for (int i = 0; i < values.length; i++) {
      values[i] *= alpha;
    }
    return this;
  }

  /**
   * Returns the inner-product of the specified DenseVector and this
   * @param vector to multiply by this
   * @return the inner-product
   */
  public double mul(DenseVector vector) {
    double ret = 0.0;
    for (int i = 0; i < Math.min(values.length, vector.values.length); i++) {
      ret += values[i] * vector.values[i];
    }
    return ret;
  }
  
  /**
   * Returns the inner-product of the specified SparseVector and this
   * @param vector to multiply by this
   * @return the inner-product
   */
  public double mul(SparseVector vector) {
    double ret = 0.0;
    for (VectorEntry e : vector) {
      if (e.index < values.length) {
        ret += values[e.index] * e.value;
      }
    }
    return ret;
  }

  /**
   * Computes the cosine similarity between the specified DenseVector and this.
   * @param vector
   * @return the cosine similarity
   */
  public double cosSim(DenseVector vector) {
    double norm = norm();
    double norm2 = vector.norm();
    if (norm == 0.0 || norm2 == 0.0) {
      return 0.0;
    }
    return mul(vector) / (norm() * vector.norm());
  }
  
  /**
   * Computes the cosine similarity between the specified SparseVector and this.
   * @param vector
   * @return the cosine similarity
   */
  public double cosSim(SparseVector vector) {
    double norm = norm();
    double norm2 = vector.norm();
    if (norm == 0.0 || norm2 == 0.0) {
      return 0.0;
    }
    return mul(vector) / (norm() * vector.norm());
  }

  /**
   * Returns the length of the vector.
   * @return the length of the vector
   */
  public int size() {
    return values.length;
  }

  /**
   * Returns the norm of the current vector (Euclidean norm).
   * @return the norm of the current vector
   */
  public double norm() {
    double norm = 0.0;
    for (int i = 0; i < values.length; i++) {
      norm += values[i] * values[i];
    }
    return Math.sqrt(norm);
  }

  /**
   * Normalizes the current vector (Euclidean norm).
   * @return this
   */
  public DenseVector normalize() {
    return mul(1.0 / norm());
  }

  /**
   * Fills the vector by 0.0.
   */
  public void clear() {
    Arrays.fill(values, 0.0);
  }

  /**
   * Returns the length of the vector.
   * @return the length of the vector
   */
  public int maxIndex() {
    return values.length;
  }
  
  /**
   * Returns the String, java Arrays.toString(...) like, representation of the current object.
   */
  public String toString() {
    return Arrays.toString(values);
  }
  
  /**
   * Lexicographically comparison of vectors.
   */
  @Override
  public int compareTo(DenseVector o) {
    for (int i = 0; i < Math.min(values.length, o.values.length); i++) {
      if (values[i] < o.values[i]) {
        return -1;
      } else if (values[i] > o.values[i]) {
        return 1;
      }
    }
    for (int i = Math.min(values.length, o.values.length); i < values.length; i++) {
      if (values[i] != 0.0) {
        return 1;
      }
    }
    for (int i = Math.min(values.length, o.values.length); i < o.values.length; i++) {
      if (o.values[i] != 0.0) {
        return -1;
      }
    }
    return 0;
  }
  
  /**
   * Iterator class for DenseVector.
   * @author István Hegedűs
   *
   */
  private class DenseVectorIterator implements Iterator<VectorEntry> {
    
    @Override
    public boolean hasNext() {
      return index < values.length-1;
    }

    @Override
    public VectorEntry next() {
      index ++;
      return new VectorEntry(index, values[index]);
    }

    @Override
    public void remove() {
      if (index == -1) {
        throw new IllegalStateException();
      }
      values[index] = 0.0;
    }
    
  }

}
