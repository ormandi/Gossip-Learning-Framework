/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gossipLearning.utils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementation of a Bloom-filter, as described here:
 * http://en.wikipedia.org/wiki/Bloom_filter
 *
 * For updates and bugfixes, see http://github.com/magnuss/java-bloomfilter
 *
 * Inspired by the SimpleBloomFilter-class written by Ian Clarke. This
 * implementation provides a more evenly distributed Hash-function by
 * using a proper digest instead of the Java RNG. Many of the changes
 * were proposed in comments in his blog:
 * http://blog.locut.us/2008/01/12/a-decent-stand-alone-java-bloom-filter-implementation/
 * 
 * The above mentioned implementation was reused for having multi-set bloom filter.
 *
 * @author István Hegedűs
 */
public class MultiBloomFilter implements Serializable {
  private static final long serialVersionUID = -1382642528679993189L;
  
  private int size;
  private double[] counters;
  private double numberOfAddedElements; // number of elements actually added to the Bloom filter
  private double numberOfDifferentElements;
  private int k; // number of hash functions
  
  static final Charset charset = Charset.forName("UTF-8");
  static final String hashName = "MD5"; // MD5 gives good enough accuracy in most circumstances. Change to SHA1 if it's needed
  static final MessageDigest digestFunction;
  static { // The digest method is reused between instances
    MessageDigest tmp;
    try {
      tmp = java.security.MessageDigest.getInstance(hashName);
    } catch (NoSuchAlgorithmException e) {
      tmp = null;
    }
    digestFunction = tmp;
  }

  /**
   * Constructs a multi-set bloom filter. </br>
   * Parameter k should be chosen as (m / expectedNumberOfElements) * Math.log(2.0)).
   * @param m the number of bits
   * @param k the number of hash functions
   */
  public MultiBloomFilter(int m, int k) {
    this.k = k;
    this.size = m;
    this.counters = new double[size];
    numberOfAddedElements = 0.0;
    numberOfDifferentElements = 0.0;
  }
  
  /**
   * Deep copy constructor.
   * @param a to be copied
   */
  public MultiBloomFilter(MultiBloomFilter a) {
    this.k = a.k;
    this.size = a.size;
    numberOfAddedElements = a.numberOfAddedElements;
    numberOfDifferentElements = a.numberOfDifferentElements;
    this.counters = new double[size];
    for (int i = 0; i < size; i++) {
      this.counters[i] = a.counters[i];
    }
  }
  
  /**
   * Makes a deep copy of the current object.
   */
  public Object clone() {
    return new MultiBloomFilter(this);
  }

  /**
   * Generates digests based on the contents of an array of bytes and splits the result into 4-byte int's and store them in an array. The
   * digest function is called until the required number of int's are produced. For each call to digest a salt
   * is prepended to the data. The salt is increased by 1 for each call.
   *
   * @param data specifies input data.
   * @param hashes number of hashes/int's to produce.
   * @return array of int-sized hashes
   */
  public static int[] createHashes(byte[] data, int hashes) {
    int[] result = new int[hashes];

    int k = 0;
    byte salt = 0;
    while (k < hashes) {
      byte[] digest;
      synchronized (digestFunction) {
        digestFunction.update(salt);
        salt++;
        digest = digestFunction.digest(data);                
      }

      for (int i = 0; i < digest.length/4 && k < hashes; i++) {
        int h = 0;
        for (int j = (i*4); j < (i*4)+4; j++) {
          h <<= 8;
          h |= ((int) digest[j]) & 0xFF;
        }
        result[k] = h;
        k++;
      }
    }
    return result;
  }

  /**
   * Compares the contents of two instances to see if they are equal.
   *
   * @param obj is the object to compare to.
   * @return True if the contents of the objects are equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultiBloomFilter other = (MultiBloomFilter) obj;        
    if (this.k != other.k) {
      return false;
    }
    if (this.size != other.size) {
      return false;
    }
    if (this.numberOfAddedElements != other.numberOfAddedElements) {
      return false;
    }
    if (this.numberOfDifferentElements != other.numberOfDifferentElements) {
      return false;
    }
    for (int i = 0; i < this.size; i++) {
      if (this.counters[i] != other.counters[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the value chosen for K.<br />
   * <br />
   * K is the optimal number of hash functions based on the size
   * of the Bloom filter and the expected number of inserted elements.
   *
   * @return optimal k.
   */
  public int getK() {
    return k;
  }

  /**
   * Sets all bits to false in the Bloom filter.
   */
  public void clear() {
    Arrays.fill(counters, 0.0);
    numberOfAddedElements = 0.0;
    numberOfDifferentElements = 0.0;
  }

  /**
   * Adds a String object to the Bloom filter. The output from the object's
   * toString() method is used as input to the hash functions.
   *
   * @param element is an element to register in the Bloom filter.
   */
  public void add(String element) {
    add(element.getBytes(charset));
  }
  
  /**
   * Adds the specified integer to the filter.
   * @param element integer to add
   */
  public void add(int element) {
    byte[] bytes = new byte[] {
        (byte)(element >>> 24),
        (byte)(element >>> 16),
        (byte)(element >>> 8),
        (byte)element
    };
    add(bytes);
  }

  /**
   * Adds an array of bytes to the Bloom filter.
   *
   * @param bytes array of bytes to add to the Bloom filter.
   */
  public void add(byte[] bytes) {
    boolean contains = true;
    int index;
    int[] hashes = createHashes(bytes, k);
    for (int hash : hashes) {
      index = Math.abs(hash % size);
      if (counters[index] == 0) {
        contains = false;
      }
      counters[index] ++;
    }
    if (!contains) {
      numberOfDifferentElements ++;
    }
    numberOfAddedElements ++;
  }

  /**
   * Returns true if the element could have been inserted into the Bloom filter.
   * Use getFalsePositiveProbability() to calculate the probability of this
   * being correct.
   *
   * @param element element to check.
   * @return true if the element could have been inserted into the Bloom filter.
   */
  public double contains(String element) {
    return contains(element.getBytes(charset));
  }
  
  /**
   * Returns true if the specified element could have been inserted into the Bloom filter.
   * Use getFalsePositiveProbability() to calculate the probability of this
   * being correct.
   * 
   * @param element integer to check
   * @return true if the element could have been inserted into the Bloom filter.
   */
  public double contains(int element) {
    byte[] bytes = new byte[] {
        (byte)(element >>> 24),
        (byte)(element >>> 16),
        (byte)(element >>> 8),
        (byte)element
    };
    return contains(bytes);
  }

  /**
   * Returns true if the array of bytes could have been inserted into the Bloom filter.
   * Use getFalsePositiveProbability() to calculate the probability of this
   * being correct.
   *
   * @param bytes array of bytes to check.
   * @return true if the array could have been inserted into the Bloom filter.
   */
  public double contains(byte[] bytes) {
    double occurrences = numberOfAddedElements;
    double counter;
    int[] hashes = createHashes(bytes, k);
    for (int hash : hashes) {
      counter = counters[Math.abs(hash % size)];
      occurrences = counter < occurrences ? counter : occurrences;
    }
    return occurrences;
  }

  /**
   * Returns the number of bits in the Bloom filter. Use count() to retrieve
   * the number of inserted elements.
   *
   * @return the size of the bitset used by the Bloom filter.
   */
  public int size() {
    return this.size;
  }

  /**
   * Returns the number of elements added to the Bloom filter after it
   * was constructed or after clear() was called.
   *
   * @return number of elements added to the Bloom filter.
   */
  public double count() {
    return this.numberOfAddedElements;
  }
  
  /**
   * Returns the number of unique elements added to the Bloom filter after it
   * was constructed or after clear() was called.
   *
   * @return number of unique elements added to the Bloom filter.
   */
  public double uniqueCount() {
    return this.numberOfDifferentElements;
  }
  
  /**
   * Calculate the probability of a false positive given the
   * number of inserted elements.
   *
   * @return probability of a false positive.
   */
  public double getFalsePositiveProbability() {
    // (1 - e^(-k * n / m)) ^ k
    return Math.pow((1 - Math.exp(-k * numberOfDifferentElements / (double) size)), k);
  }

  /**
   * Java Map like string representation.
   */
  public String toString() {
    boolean contains = false;
    StringBuffer sb = new StringBuffer();
    sb.append('{');
    for (int i = 0; i < size; i++) {
      if (counters[i] != 0.0) {
        if (contains) {
          sb.append(',');
          sb.append(' ');
        } else {
          contains = true;
        }
        sb.append(i);
        sb.append('=');
        sb.append(counters[i]);
      }
    }
    sb.append('}');
    return sb.toString();
  }
  
}