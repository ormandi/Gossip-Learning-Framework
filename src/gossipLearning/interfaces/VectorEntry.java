package gossipLearning.interfaces;

import java.io.Serializable;

/**
 * This class represents an index value pair for the iterators of 
 * the sparse and dense vectors.
 * @author István Hegedűs
 *
 */
public class VectorEntry implements Serializable{
  private static final long serialVersionUID = 602724499869617224L;
  
  /**
   * The index of the vector entry.
   */
  public int index;
  /**
   * The value of the vector entry.
   */
  public double value;
  
  /**
   * Constructor that initializes the index and the value member variables 
   * based on the specified parameters.
   * @param index index to be set.
   * @param value value to be set.
   */
  public VectorEntry(int index, double value) {
    this.index = index;
    this.value = value;
  }
  public String toString() {
    return index + " " + value;
  }
}
