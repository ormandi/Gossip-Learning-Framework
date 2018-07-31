package gossipLearning.utils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

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
  
  /**
   * Constructs a SparseVector instance with capacity 16, growing factor 1.5.
   */
  public SparseVector() {
    this(defaultCapacity, defaultGrowFactor);
  }
  
  /**
   * Constructs a SparseVector instance with the specified capacity and 
   * default grow factor.
   * @param capacity capacity of the vector
   */
  public SparseVector(int capacity) {
    this(capacity < 1 ? defaultCapacity : capacity, defaultGrowFactor);
  }
  
  /**
   * Constructs a SparseVector instance with default capacity and 
   * the specified grow factor.
   * @param capacity capacity of the vector
   */
  public SparseVector(double growFactor) {
    this(defaultCapacity, growFactor <= 1.0 ? defaultGrowFactor : growFactor);
  }
  
  /**
   * Constructs a SparseVector instance with the specified capacity and 
   * the specified grow factor.
   * @param capacity capacity of the vector
   * @param growFactor the factor of the growth
   */
  public SparseVector(int capacity, double growFactor) {
    if (capacity < 1) {
      indices = new int[defaultCapacity];
      values = new double[defaultCapacity];
    } else {
      indices = new int[capacity];
      values = new double[capacity];
    }
    size = 0;
    this.growFactor = growFactor <= 1.0 ? defaultGrowFactor : growFactor;
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
   * Constructs a SparseVector from the specified indices and corresponding values
   * vectors.
   * @param indices indices to store
   * @param values values to be stored
   */
  public SparseVector(int[] indices, double[] values) {
    if (indices.length != values.length) {
      throw new RuntimeException("Can not create vector with different size of indices and values: " + indices.length + "<->" + values.length);
    }
    this.growFactor = defaultGrowFactor;
    this.indices = new int[indices.length];
    this.values = new double[values.length];
    this.size = 0;
    for (int i = 0; i < indices.length; i++) {
      if (values[i] != sparseValue) {
        if (i > 0 && indices[i] <= indices[i - 1]) {
          throw new RuntimeException("The indices have to be in ascendent order.");
        }
        this.indices[size] = indices[i];
        this.values[size] = values[i];
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
      }
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
    if (size != ((SparseVector)vector).size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (indices[i] != ((SparseVector)vector).indices[i] || 
          values[i] != ((SparseVector)vector).values[i])
        return false;
    }
    return true;
  }
  
  /**
   * Sets the values of the specified vector to the values of the current 
   * vector.
   * @param vector to be set.
   * @return this
   */
  public SparseVector set(SparseVector vector) {
    if (indices.length < vector.indices.length) {
      indices = new int[vector.indices.length];
      values = new double[vector.values.length];
    }
    System.arraycopy(vector.indices, 0, indices, 0, vector.indices.length);
    System.arraycopy(vector.values, 0, values, 0, vector.values.length);
    size = vector.size;
    for (int i = size; i < indices.length; i++) {
      indices[i] = 0;
      values[i] = sparseValue;
    }
    return this;
  }
  
  @Override
  public Iterator<VectorEntry> iterator() {
    return new SparseVectorIterator(this);
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
   * Shifts the values forward to fill the space of the specified unnecessary index.
   * @param idx index to be removed
   */
  private void removeIdx(int idx) {
    for (int i = idx; i < size -1; i++) {
      indices[i] = indices[i + 1];
      values[i] = values[i + 1];
    }
    indices[size-1] = 0;
    values[size-1] = sparseValue;
    size --;
  }
  
  /**
   * Returns the position of the specified index in the array.
   * @param index index to looking for
   * @return the position of the specified index
   */
  private int getIdx(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("" + index);
    }
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
    return idx < 0 ? sparseValue : values[idx];
  }

  /**
   * Stores the specified value at the specified index. The value, that is stored, at 
   * the specified index will be overridden.
   * @param index index to store at
   * @param value value to be stored
   * @return this
   */
  public SparseVector put(int index, double value) {
    // get the position of insertion
    int idx = getIdx(index);
    if (idx >= 0) {
      // the container contains the value at index
      // if the insertion is default value then remove
      if (value == sparseValue) {
        removeIdx(idx);
        return this;
      }
      // insert the value
      values[idx] = value;
    } else {
      // if the container does not contain value at index
      if (value == sparseValue) {
        // if the value is the default return
        return this;
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
    return this;
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
      return res;
    }
    return sparseValue;
  }

  /**
   * Adds the specified value to the value at the specified index.
   * @param index index of value to add to
   * @param value value to be added
   */
  public SparseVector add(int index, double value) {
    int idx = getIdx(index);
    if (idx >= 0) {
      if (value != sparseValue) {
        values[idx] += value;
      }
      if (values[idx] == sparseValue) {
        removeIdx(idx);
      }
    } else {
      if (value == sparseValue) {
        return this;
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
    return this;
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
   * Adds the specified vector to the vector that is represented by the current object. 
   * @param vector to be added
   * @return the sum of the specified vector and this
   */
  public SparseVector add(double[] vector) {
    return add(vector, 1.0);
  }
  
  /**
   * Adds the specified vector to the vector, that is represented by the current 
   * object, by the specified alpha times.
   * @param vector to be added
   * @param alpha scale factor of the addition
   * @return the sum of this and the alpha times of the vector
   */
  public SparseVector add(double[] vector, double alpha) {
    int idx = 0;
    for (int i = 0; i < vector.length; i++) {
      if (size <= idx) {
        add(i, vector[i] * alpha);
      } else if (indices[idx] == i) {
        values[idx] += vector[i] * alpha;
        if (values[idx] == sparseValue) {
          removeIdx(idx);
        } else {
          idx ++;
        }
      } else if (i < indices[idx]) {
        add(i, vector[i] * alpha);
        idx ++;
      }
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
  * Point-wise multiplies the current vector by the specified sparse vector.
  * @param vector to multiply with
  * @return this
  */
    public SparseVector pointMul(SparseVector vector) {
      int idx = 0;
      int idx2 = 0;
      while (idx < size && idx2 < vector.size) {
        if (indices[idx] == vector.indices[idx2]) {
          values[idx] = vector.values[idx2] * values[idx];
          idx ++;
          idx2 ++;
        } else if (indices[idx] < vector.indices[idx2]) {
          remove(indices[idx]);
        } else {
          idx2 ++;
        }
      }
      return this;
    }
  
  /**
   * Point-wise divides the current vector by the specified sparse vector.
   * @note Division by 0 will be result 0!
   * @param vector to divide with
   * @return this
   */
  public SparseVector div(SparseVector vector) {
    int idx = 0;
    int idx2 = 0;
    while (idx < size && idx2 < vector.size) {
      if (indices[idx] == vector.indices[idx2]) {
        values[idx] = values[idx] / vector.values[idx2];
        idx ++;
        idx2 ++;
      } else if (indices[idx] < vector.indices[idx2]) {
        remove(indices[idx]);
      } else {
        idx2 ++;
      }
    }
    return this;
  }
  
  /**
   * Returns the stored index at the specified position.
   * @param index position of the index to be returned
   * @return the index
   */
  public int indexAt(int index) {
    return indices[index];
  }
  
  /**
   * Returns the stored value at the specified position.
   * @param index position of the value to be returned
   * @return the value
   */
  public double valueAt(int index) {
    return values[index];
  }
  
  /**
   * Returns the number of stored values.
   * @return the number of stored values
   */
  public int size() {
    return size;
  }

  /**
   * Removes the elements from the vector.
   * @return this
   */
  public SparseVector clear() {
    for (int i = 0; i < size; i++) {
      indices[i] = 0;
      values[i] = sparseValue;
    }
    size = 0;
    return this;
  }

  /**
   * Computes the cosine similarity between the specified SparseVector and this.
   * @param vector
   * @return the cosine similarity
   */
  public double cosSim(SparseVector vector) {
    double norm = this.norm();
    double norm2 = vector.norm();
    if (norm == 0.0 || norm2 == 0.0) {
      return 0.0;
    }
    return this.mul(vector) / (norm * norm2);
  }
  
  /**
   * Computes the Euclidean distance between the specified SparseVector and this.
   * @param vector
   * @return the Euclidean distance
   */
  public double euclideanDistance(SparseVector vector) {
    double dist = 0.0;
    int idx = 0;
    int idx2 = 0;
    while (idx < size && idx2 < vector.size) {
      if (indices[idx] == vector.indices[idx2]) {
        dist = Utils.hypot(dist, values[idx] - vector.values[idx2]);
        idx ++;
        idx2 ++;
      } else if (indices[idx] < vector.indices[idx2]) {
        dist = Utils.hypot(dist, values[idx]);
        idx ++;
      } else {
        dist = Utils.hypot(dist, vector.values[idx2]);
        idx2 ++;
      }
    }
    for (int i = idx2; i < vector.size; i++) {
      dist = Utils.hypot(dist, vector.values[i]);
    }
    for (int i = idx; i < size; i++) {
      dist = Utils.hypot(dist, values[i]);
    }
    return dist;
  }
  
  /**
   * Returns the infinite norm of the current vector.
   * @return the infinite norm of the current vector
   */
  public double norminf() {
    double norm = 0.0;
    for (int i = 0; i < size; i++) {
      if (norm < Math.abs(values[i])) {
        norm = Math.abs(values[i]);
      }
    }
    return norm;
  }
  
  /**
   * Returns the norm of the current vector (Euclidean norm).
   * @return the norm of the current vector
   */
  public double norm() {
    double norm = 0.0;
    for (int i = 0; i < size; i++) {
      norm = Utils.hypot(norm, values[i]);
    }
    return norm;
  }
  
  /**
   * Returns the norm 1 of the current vector.
   * @return the norm 1 of the current vector
   */
  public double norm1() {
    double norm = 0.0;
    for (int i = 0; i < size; i++) {
      norm += Math.abs(values[i]);
    }
    return norm;
  }

  /**
   * Normalizes the current vector (Euclidean norm).
   * @return this
   */
  public SparseVector normalize() {
    double norm = this.norm();
    if (norm > 0.0 ) {
      mul(1.0 / norm);
    }
    return this;
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
   * Returns the sum of the vector elements.
   * @return the sum of the elements
   */
  public double sum() {
    double sum = 0.0;
    for (int i = 0; i < size; i++) {
      sum += values[i];
    }
    return sum;
  }
  
  /**
   * Performs squared root on every values in the vector;
   * @return this
   */
  public SparseVector sqrt() {
    for (int i = 0; i < size; i++) {
      values[i] = Math.sqrt(values[i]);
    }
    return this;
  }
  
  /**
   * Point-wise inverts the non 0 vector elements.
   * @return this
   */
  public SparseVector inv() {
    for (int i = 0; i < size; i++) {
      values[i] = 1.0f / values[i];
    }
    return this;
  }
  
  /**
   * Sets the values point-wise to the specified power.
   * @param power
   * @return this
   */
  public SparseVector powerTo(double power) {
    for (int i = 0; i < size; i++) {
      values[i] = Math.pow(values[i], power);
    }
    return this;
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
  
  public SparseVector scaleValueRange(int nbits, Random r) {
    int idx = 0;
    while (idx < size) {
      assert values[idx] <= 1 && values[idx] >= -1;
      values[idx] = Utils.scaleValueRange(values[idx], nbits, r);
      if (values[idx] == sparseValue) {
        removeIdx(idx);
      } else {
        idx ++;
      }
    }
    return this;
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
    
    private final SparseVector vector;
    private int index;
    
    public SparseVectorIterator(SparseVector vector) {
      this.vector = vector;
      index = -1;
    }
    
    @Override
    public boolean hasNext() {
      return index < vector.size-1;
    }

    @Override
    public VectorEntry next() {
      index ++;
      return new VectorEntry(vector.indices[index], vector.values[index]);
    }

    @Override
    public void remove() {
      if (index == -1) {
        throw new IllegalStateException();
      }
      vector.remove(vector.indices[index]);
    }
    
  }

}
