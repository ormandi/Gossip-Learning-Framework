package gossipLearning.interfaces.protocols;

import gossipLearning.utils.InstanceHolder;

public interface DimensionReductionProtocol {
  
  /**
   * Returns the instances that have the extracted features.
   * @return the extracted instances
   */
  public InstanceHolder getInstances();
  
}
