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
  public LearningModel clone();
  /**
   * Updates the actual model with a training instance.
   * @param instance the features that represent the instance
   * @param label the class label of the training instance or Double.NaN in case of clustering
   */
  public void update(SparseVector instance, double label);
  
  /**
   * Updates the model using the specified batch of training instances.
   * @param instances batch of training instances
   */
  public void update(InstanceHolder instances);
  
  /**
   * Updates the model by using the specified training instances, runs the 
   * specified epoch, with the specified size of batches. 
   * @param instances used for update
   * @param epoch number of update rounds
   * @param batchSize mini-batch size
   */
  public void update(InstanceHolder instances, int epoch, int batchSize);
  
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
