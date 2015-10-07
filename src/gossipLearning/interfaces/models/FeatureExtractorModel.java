package gossipLearning.interfaces.models;

import gossipLearning.utils.SparseVector;

/**
 * This interface describes a dynamic feature manipulator class 
 * (e.g. feature normalization).
 *  
 * @author István Hegedűs
 */
public interface FeatureExtractorModel extends FeatureExtractor {
  /**
   * Updates the model by the specified instance.
   * @param instance for update the model
   */
  public void update(SparseVector instance, double label);
  
  public FeatureExtractorModel merge(FeatureExtractorModel model);
}
