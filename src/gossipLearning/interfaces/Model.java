package gossipLearning.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface describes the models that can be used in our system.
 * These models should be online updatable.
 * @author Istvan Hegedus
 *
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
   * This method updates the actual model with a training instance.
   * @param instance the features that represent the instance
   * @param label the class label of the training instance or Double.NaN in case of clustering
   */
  public void update(final Map<Integer, Double> instance, final double label);
  
  /**
   * This method can predict the label or the category of a given evaluation instance.
   * @param instance the features that represent the instance
   * @return returns the class label or the category that was predicted by the model
   */
  public double predict(final Map<Integer, Double> instance);
}
