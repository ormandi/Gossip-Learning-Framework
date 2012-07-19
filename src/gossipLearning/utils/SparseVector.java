package gossipLearning.utils;

import gossipLearning.interfaces.VectorEntry;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements a sparse vector by arrays and sparse for the 0.0 value. <br/>
 * For get, put and remove methods uses O(log(n)) time. The iteration is 
 * linear time and enumerates the indices in ascendant order. <br/>
 * The default size of this container is 16. If the container is full, it grows 
 * automatically by a factor, that is 1.5 by default.
 * @author István Hegedűs
 *
 */
public class SparseVector implements Serializable, Iterable<VectorEntry>, Comparable<SparseVector> {
  private static final long serialVersionUID = 5601072455432194047L;
  
  private static final int defaultCapacity = 16;
  private static final double sparseValue = 0.0;
  private static final double defaultGrowFactor = 1.5;

  private final double growFactor;
  
  private int[] indices;
  private double[] values;
  private int size;
  
  private int index;
  
  /**
   * Constructs a SparseVector instance with capacity 16, growing factor 1.5.
   */
  public SparseVector() {
    this(defaultCapacity);
  }
  
  /**
   * Constructs a SparseVector instance with the specified capacity and 
   * default grow factor.
   * @param capacity capacity of the vector
   */
  public SparseVector(int capacity) {
    this(capacity, defaultGrowFactor);
  }
  
  /**
   * Constructs a SparseVector instance with the specified capacity and 
   * the specified grow factor.
   * @param capacity capacity of the vector
   * @param growFactor the factor of the growth
   */
  public SparseVector(int capacity, double growFactor) {
    indices = new int[capacity];
    values = new double[capacity];
    size = 0;
    index = Integer.MIN_VALUE;
    this.growFactor = growFactor;
  }
  
  /**
   * Constructs a SparseVector by makes a deep copy of the specified vector.
   * @param vector vector to be cloned
   */
  public SparseVector(SparseVector vector) {
    this(vector.indices.length, vector.growFactor);
    size = vector.size;
    System.arraycopy(vector.indices, 0, indices, 0, size);
    System.arraycopy(vector.values, 0, values, 0, size);
    index = vector.index;
  }
  
  /**
   * Constructs a SparseVector from the specified double array vector.
   * @param vector array to be stored
   */
  public SparseVector(double[] vector) {
    this(vector.length);
    for (int i = 0; i < vector.length; i++) {
      if (vector[i] != sparseValue) {
        indices[size] = i;
        values[size] = vector[i];
        size ++;
      }
    }
  }
  
  /**
   * Constructs a SparseVector from the specified Map<Integer, Double> vector.
   * @param vector array to be stored
   */
  public SparseVector(Map<Integer, Double> vector) {
    this(vector.size());
    for (Map.Entry<Integer, Double> e : vector.entrySet()) {
      if (e.getValue() != sparseValue) {
        put(e.getKey(), e.getValue());
        size ++;
      }
    }
  }
  
  /**
   * Constructs a SparseVector from the specified DenseVector vector.
   * @param vector vector to be stored
   */
  public SparseVector(DenseVector vector) {
    this();
    for (int i = 0; i < vector.size(); i++) {
      put(i, vector.get(i));
    }
  }
  
  /**
   * Makes a deep copy of the current vector.
   */
  public Object clone() {
    return new SparseVector(this);
  }
  
  /**
   * Two sparse vectors are equal if have the same size and have the same values at 
   * the same positions.
   */
  public boolean equals(Object vector) {
    if (!(vector instanceof SparseVector)) {
      return false;
    }
    if (size != ((SparseVector)vector).size()) {
      return false;
    }
    for (VectorEntry e : (SparseVector)vector) {
      if (e.value != get(e.index)) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public Iterator<VectorEntry> iterator() {
    index = -1;
    return new SparseVectorIterator();
  }

  /**
   * Returns the position of the specified index in the array.
   * @param index index to looking for
   * @return the position of the specified index
   */
  private int getIdx(int index) {
    int first = 0;
    int last = size - 1;
    while(first <= last) {
      int med = (first + last) >>> 1;
      if (indices[med] < index) {
        first = med + 1;
      } else if (indices[med] > index) {
        last = med - 1;
      } else {
        return med;
      }
    }
    return -(first + 1);
  }
  
  /**
   * Returns the value that is stored at the specified index.
   * @param index index of the value to be returned
   * @return value at the specified index
   */
  public double get(int index) {
    int idx = getIdx(index);
    return idx < 0 ? 0.0 : values[idx];
  }

  /**
   * Resizes the vector by the factor of growth.
   */
  private void grow() {
    int capacity = Math.max((int)(indices.length * growFactor), indices.length + 1);
    int[] newIndices = new int[capacity];
    double[] newValues = new double[capacity];
    System.arraycopy(indices, 0, newIndices, 0, size);
    System.arraycopy(values, 0, newValues, 0, size);
    indices = newIndices;
    values = newValues;
  }
  
  /**
   * Stores the specified value at the specified index. The value, that is stored, at 
   * the specified index will be overridden.
   * @param index index to store at
   * @param value value to be stored
   */
  public void put(int index, double value) {
    // get the position of insertion
    int idx = getIdx(index);
    if (idx >= 0) {
      // the container contains the value at index
      // if the insertion is default value then remove
      if (value == sparseValue) {
        removeIdx(idx);
        size --;
        return;
      }
      // insert the value
      values[idx] = value;
    } else {
      // if the container does not contain value at index
      if (value == sparseValue) {
        // if the value is the default return
        return;
      }
      if (size >= indices.length) {
        // if the container is full, then grow
        grow();
      }
      // the insertion index can be computed from the result of the getIndex
      idx = -idx - 1;
      // slide the indices and the values
      for (int i = size -1; i >= idx; i--) {
        indices[i + 1] = indices[i];
        values[i + 1] = values[i];
      }
      // insert new element
      indices[idx] = index;
      values[idx] = value;
      size ++;
    }
  }

  /**
   * Removes and returns the value at the specified index.
   * @param index index of value to be removed
   * @return the value that was removed
   */
  private double delete(int index) {
    return remove(index);
  }
  
  
  /**
   * Removes and returns the value at the specified index.
   * @param index index of value to be removed
   * @return the value that was removed
   */
  public double remove(int index) {
    int idx = getIdx(index);
    if (idx >= 0) {
      double res = values[idx];
      removeIdx(idx);
      size --;
      return res;
    }
    return sparseValue;
  }

  /**
   * Adds the specified SparseVector to the vector that is represented by the current object. 
   * @param vector to be added
   * @return the sum of the specified vector and this
   */
  public SparseVector add(SparseVector vector) {
    return add(vector, 1.0);
  }
  
  /**
   * Adds the specified DenseVector to the vector that is represented by the current object. 
   * @param vector to be added
   * @return the sum of the specified vector and this
   */
  public SparseVector add(DenseVector vector) {
    return add(vector, 1.0);
  }
  
  /**
   * Shifts the values forward to fill the space of the specified unnecessary index.
   * @param idx index to be removed
   */
  private void removeIdx(int idx) {
    for (int i = idx; i < size -1; i++) {
      indices[i] = indices[i + 1];
      values[i] = values[i + 1];
    }
  }
  
  /**
   * Adds the specified value to the value at the specified index.
   * @param index index of value to add to
   * @param value value to be added
   */
  private void add(int index, double value) {
    int idx = getIdx(index);
    if (idx >= 0) {
      if (value != sparseValue) {
        values[idx] += value;
      }
      if (values[idx] == sparseValue) {
        removeIdx(idx);
        size --;
      }
    } else {
      if (value == sparseValue) {
        return;
      }
      if (size >= indices.length) {
        grow();
      }
      idx = -idx - 1;
      for (int i = size -1; i >= idx; i--) {
        indices[i + 1] = indices[i];
        values[i + 1] = values[i];
      }
      indices[idx] = index;
      values[idx] = value;
      size ++;
    }
  }

  /**
   * Adds the specified SparseVector to the vector, that is represented by the current 
   * object, by the specified alpha times.
   * @param vector to be added
   * @param alpha scale factor of the addition
   * @return the sum of this and the alpha times of the vector
   */
  public SparseVector add(SparseVector vector, double alpha) {
    int idx = 0;
    int idx2 = 0;
    while (idx < size && idx2 < vector.size) {
      if (indices[idx] == vector.indices[idx2]) {
        values[idx] += vector.values[idx2] * alpha;
        if (values[idx] == sparseValue) {
          removeIdx(idx);
          size --;
        } else {
          idx ++;
        }
        idx2 ++;
      } else if (indices[idx] < vector.indices[idx2]) {
        idx ++;
      } else {
        put(vector.indices[idx2], vector.values[idx2] * alpha);
        idx2 ++;
      }
    }
    for (int i = idx2; i < vector.size; i++) {
      put(vector.indices[i],vector.values[i] * alpha);
    }
    return this;
  }
  
  /**
   * Adds the specified DenseVector to the vector, that is represented by the current 
   * object, by the specified alpha times.
   * @param vector to be added
   * @param alpha scale factor of the addition
   * @return the sum of this and the alpha times of the vector
   */
  public SparseVector add(DenseVector vector, double alpha) {
    if (alpha == 0.0) {
      return this;
    }
    for (int i = 0; i < vector.size(); i++) {
      add(i, vector.get(i) * alpha);
    }
    return this;
  }
  
  /**
   * Scales the current vector by the specified value.
   * @param alpha the scale factor
   * @return this
   */
  public SparseVector mul(double alpha) {
    if (alpha == 0.0) {
      clear();
    }
    for (int i = 0; i < size; i++) {
      values[i] *= alpha;
    }
    return this;
  }

  /**
   * Returns the inner-product of the specified SparseVector and this
   * @param vector to multiply by this
   * @return the inner-product
   */
  public double mul(SparseVector vector) {
    int idx = 0;
    int idx2 = 0;
    double result = 0.0;
    while (idx < size && idx2 < vector.size) {
      if (indices[idx] == vector.indices[idx2]) {
        result += values[idx] * vector.values[idx2];
        idx ++;
        idx2 ++;
      } else if (indices[idx] < vector.indices[idx2]) {
        idx ++;
      } else {
        idx2 ++;
      }
    }
    return result;
  }
  
  /**
   * Returns the inner-product of the specified DenseVector and this
   * @param vector to multiply by this
   * @return the inner-product
   */
  public double mul(DenseVector vector) {
    double result = 0.0;
    for (int i = 0; i < size; i++) {
      if (indices[i] < vector.size()) {
        result += values[i] * vector.get(indices[i]);
      }
    }
    return result;
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
   * Computes the Euclidean distance between the specified SparseVector and this.
   * @param vector
   * @return the Euclidean distance
   */
  public double euclideanDistance(SparseVector vector) {
    SparseVector clone = (SparseVector)clone();
    clone.add(vector, -1.0);
    return clone.norm();
  }
  
  /**
   * Computes the Euclidean distance between the specified DenseVector and this.
   * @param vector
   * @return the Euclidean distance
   */
  public double euclideanDistance(DenseVector vector) {
    SparseVector clone = (SparseVector)clone();
    clone.add(vector, -1.0);
    return clone.norm();
  }

  /**
   * Returns the number of stored values.
   * @return the number of stored values
   */
  public int size() {
    return size;
  }

  /**
   * Returns the norm of the current vector (Euclidean norm).
   * @return the norm of the current vector
   */
  public double norm() {
    double norm = 0.0;
    for (int i = 0; i < size; i++) {
      norm += values[i] * values[i];
    }
    return Math.sqrt(norm);
  }

  /**
   * Normalizes the current vector (Euclidean norm).
   * @return this
   */
  public SparseVector normalize() {
    double norm = norm();
    if (norm > 0.0 ) {
      mul(1.0 / norm);
    }
    return this;
  }

  /**
   * Removes the elements from the vector.
   */
  public void clear() {
    size = 0;
  }

  /**
   * Returns the maximal index of value that is stored in the vector or -1 if the 
   * vector is empty.
   * @return maximal stored index
   */
  public int maxIndex() {
    return size == 0 ? -1 : indices[size -1];
  }
  
  /**
   * Returns the String, java Map like, representation of the current object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append('{');
    for (int i = 0; i < size; i++) {
      if (i != 0) {
        sb.append(',');
        sb.append(' ');
      }
      sb.append(indices[i]);
      sb.append('=');
      sb.append(values[i]);
    }
    sb.append('}');
    return sb.toString();
  }

  /**
   * Lexicographically comparison of vectors.
   */
  @Override
  public int compareTo(SparseVector o) {
    int idx = 0;
    int idx2 = 0;
    while (idx < size && idx2 < o.size) {
      if (indices[idx] == o.indices[idx2]) {
        if (values[idx] < o.values[idx2]) {
          return -1;
        } else if (values[idx] > o.values[idx2]) {
          return 1;
        }
        idx ++;
        idx2 ++;
      } else if (indices[idx] < o.indices[idx2]) {
        return 1;
      } else {
        return -1;
      }
    }
    if (idx < size) {
      return 1;
    }
    if (idx2 < o.size) {
      return -1;
    }
    return 0;
  }
  
  /**
   * Iterator class for SparseVector.
   * @author István Hegedűs
   *
   */
  private class SparseVectorIterator implements Iterator<VectorEntry> {
    
    @Override
    public boolean hasNext() {
      return index < size-1;
    }

    @Override
    public VectorEntry next() {
      index ++;
      return new VectorEntry(indices[index], values[index]);
    }

    @Override
    public void remove() {
      if (index == -1) {
        throw new IllegalStateException();
      }
      delete(indices[index]);
    }
    
  }

}
