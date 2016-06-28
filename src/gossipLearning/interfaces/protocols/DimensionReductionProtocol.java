package gossipLearning.interfaces.protocols;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.InstanceHolder;

public interface DimensionReductionProtocol {
  
  /**
   * Returns the instances that have the extracted features.
   * @return the extracted instances
   */
  public InstanceHolder getInstances();
  public FeatureExtractor getModel();
  public InstanceHolder getInstanceHolder();
  public void setInstanceHolder(InstanceHolder instances);
  
}
