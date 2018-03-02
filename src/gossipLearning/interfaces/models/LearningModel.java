package gossipLearning.interfaces.models;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

/**
 * This interface describes the models that can be used for learning.
 * These models should be online updatable.
 * 
 * @author István Hegedűs
 */
public interface LearningModel extends Model {
  /**
   * This method updates the actual model with a training instance.
   * @param instance the features that represent the instance
   * @param label the class label of the training instance or Double.NaN in case of clustering
   */
  public void update(SparseVector instance, double label);
  
  /**
   * This method updates the model using the specified batch of training instances.
   * @param instances batch of training instances
   */
  public void update(InstanceHolder instances);
  
  /**
   * This method can predict the label or the category of a given evaluation instance.
   * @param instance the features that represent the instance
   * @return returns the class label or the category that was predicted by the model
   */
  public double predict(SparseVector instance);
  
  /**
   * Sets the number of classes that will be used during the training phase 
   * and the number features of the instances.
   * @param numberOfClasses number of classes
   * @param numberOfFeatures number of features
   */
  public void setParameters(int numberOfClasses, int numberOfFeatures);
}
