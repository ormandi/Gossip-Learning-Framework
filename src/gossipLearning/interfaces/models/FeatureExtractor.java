package gossipLearning.interfaces.models;

import gossipLearning.utils.InstanceHolder;

/**
 * This interface describes a static (local) feature manipulator class 
 * (e.g. polynomial features).
 *  
 * @author István Hegedűs
 */
public interface FeatureExtractor extends Model {
  /**
   * Applies the feature manipulation for the specified instances.
   * @param instances to be manipulated
   * @return manipulated instances
   */
  public InstanceHolder extract(InstanceHolder instances);
}
