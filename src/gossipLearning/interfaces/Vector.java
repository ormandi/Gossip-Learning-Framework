package gossipLearning.interfaces;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import gossipLearning.utils.VectorEntry;

public abstract class Vector implements Serializable, Iterable<VectorEntry>, Comparable<Vector> {
  private static final long serialVersionUID = 3952370738088622304L;
  /** The initial capacity of the vector. */
  public static final int defaultCapacity = 16;
  
  /**
   * Returns the deep copy of the object.
   * @return deep copy
   */
  public abstract Vector clone();
  
  /**
   * Returns true iff the specified object has the same type and the contains 
   * the same values as the current object.
   * @param o to be checked
   * @return true iff type is matched and have same values
   */
  public abstract boolean equals(Object o);
  
  /**
   * Define lexicographical ordering.
   */
  public abstract int compareTo(Vector vector);
  
  public abstract Iterator<VectorEntry> iterator();
  
  /**
   * Removes the stored values from the vector.
   * @return this
   */
  public abstract Vector clear();
  
  /**
   * Stores the specified value at the specified index in the vector.
   * @param index to be set at
   * @param value to be set
   * @return this
   */
  public abstract Vector put(int index, double value);
  
  /**
   * Returns the value stored at the specified index in the vector.
   * @param index to be get at
   * @return stored value
   */
  public abstract double get(int index);
  
  /**
   * Removes and returns the value stored at the specified index in the vector.
   * @param index to be removed at
   * @return stored value
   */
  public abstract double remove(int index);
  
  /**
   * Returns the number of stored values in the vector.
   * @return number of stored value
   */
  public abstract int size();
  
  /**
   * Returns the length of the vector, the maximum of the stored indices + 1.
   * @return maximal index + 1
   */
  public abstract int length();
  
  /**
   * Sets the values of the specified vector as the values of the current vector.
   * @param vector to be set
   * @return this
   */
  public Vector set(Vector vector) {
    clear();
    return add(vector);
  }
  
  /**
   * Adds the specified value to the value of the vector stored at the specified 
   * index.
   * @param index to be added to at
   * @param value to be added
   * @return this
   */
  public abstract Vector add(int index, double value);
  
  /**
   * Adds the values of the specified array to the current vector.
   * @param vector to be added
   * @return this
   */
  public abstract Vector add(double[] vector);
  
  /**
   * Adds the values of the specified array to the current vector multiplied by 
   * the specified value.
   * @param vector to be added
   * @param times multiplication factor
   * @return this
   */
  public abstract Vector add(double[] vector, double times);
  
  /**
   * Adds the values of the specified vector to the current vector.
   * @param vector to be added
   * @return this
   */
  public final Vector add(Vector vector) {
    return add(vector, 1.0);
  }
  
  /**
   * 
   * Adds the values of the specified vector to the current vector multiplied by 
   * the specified value.
   * @param vector to be added
   * @param times multiplication factor
   * @return this
   */
  public Vector add(Vector vector, double times) {
    for (int i = 0; i < vector.length(); i++) {
      add(i, times * vector.get(i));
    }
    return this;
  }
  
  /**
   * Multiplies the values of the current vector by the specified value.
   * @param value multiplication factor
   * @return this
   */
  public abstract Vector mul(double value);
  
  /**
   * Returns the inner-product of the specified and the current vectors.
   * @param vector to multiply with
   * @return inner-product
   */
  public double mul(Vector vector) {
    double result = 0.0;
    for (int i = 0; i < vector.length(); i++) {
      result += get(i) * vector.get(i);
    }
    return result;
  }
  
  /**
   * Multiplies the current vector point-wise by the specified vector. 
   * @param vector to multiply with
   * @return this
   */
  public Vector pointMul(Vector vector) {
    for (int i = 0; i < length(); i++) {
      put(i, get(i) * vector.get(i));
    }
    return this;
  }
  
  /**
   * Divides the current vector point-wise by the specified vector.
   * Note: division by 0 results 0!
   * @param vector to divide with
   * @return this
   */
  public Vector div(Vector vector) {
    for (int i = 0; i < length(); i++) {
      double v = vector.get(i);
      put(i, v == 0.0 ? 0.0 : get(i) / v);
    }
    return this;
  }
  
  /**
   * Inverts the values of the vector, 0 remains 0.
   * @return this
   */
  public abstract Vector invert();
  
  /**
   * Raises the values of the vector to the specified power.
   * @param power raise to
   * @return this
   */
  public abstract Vector powerTo(double power);
  
  /**
   * Scales (unbiased) the values of the vector in order to each value can be 
   * stored in the specified number of bits. The values of the vector have to be 
   * in [0, 1] range. The specified random number generator is used to have 
   * unbiased scaling.
   * @param bits specifies the precision
   * @param random to have unbiased scaling
   * @return this
   */
  public abstract Vector scale(int bits, Random random);
  
  /**
   * Returns the cosine similarity between the current and the specified vectors.
   * @param vector compute with
   * @return cosine similarity
   */
  public final double cosineSimilarity(Vector vector) {
    double norm = norm2();
    double other = vector.norm2();
    if (norm == 0.0 || other == 0.0) {
      return 0.0;
    }
    return mul(vector) / norm2() / vector.norm2();
  }
  
  /**
   * Returns the euclidean distance between the current and the specified vectors.
   * @param vector compute with
   * @return euclidean distance
   */
  public abstract double euclideanDistance(Vector vector);
  
  /**
   * Returns the L1 norm of the vector.
   * @return L1 norm
   */
  public abstract double norm1();
  
  /**
   * Returns the L2 norm of the vector.
   * @return L2 norm
   */
  public abstract double norm2();
  
  /**
   * Returns the infinite norm of the vector.
   * @return infinite norm
   */
  public abstract double normInf();
  
  /**
   * Returns the sum of the values of the vector.
   * @return sum of values
   */
  public abstract double sum();
  
  public abstract String toString();
}
