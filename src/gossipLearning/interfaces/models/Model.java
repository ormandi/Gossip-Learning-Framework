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
   * This method is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public void init(String prefix);
  
  /**
   * Returns the age (the number of updates) of the model.
   * @return the age
   */
  public double getAge();
}
