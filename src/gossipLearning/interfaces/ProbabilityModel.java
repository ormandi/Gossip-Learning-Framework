package gossipLearning.interfaces;

import java.util.Map;

/**
 * Such a kind of model that can return the distribution of the class labels 
 * for the predictable instance.
 * @author István Hegedűs
 *
 */
public interface ProbabilityModel {
  /**
   * Returns the distribution of the class labels for the specified instance.<br/><br/>
   * <b>NOTE:</b> We do not expect the real distribution here. The only requirement is 
   * the index of the maximal value corresponds to the most likely class.
   * @param instance instance for computing distribution
   * @return array of distribution
   */
  public double[] distributionForInstance(Map<Integer, Double> instance);
}
