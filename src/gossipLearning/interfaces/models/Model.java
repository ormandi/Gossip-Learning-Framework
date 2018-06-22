package gossipLearning.interfaces.models;

import java.io.Serializable;

/**
 * This interface describes the models that can be used in our system.
 * 
 * @author István Hegedűs
 */
public interface Model extends Serializable, Cloneable {
  /**
   * Returns a clone of this object.
   * @return the clone of this object
   */
  public Object clone();
  
  /**
   * Returns the age (the number of updates) of the model.
   * @return the age
   */
  public double getAge();
  
}
