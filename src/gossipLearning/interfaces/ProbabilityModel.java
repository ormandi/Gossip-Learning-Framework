package gossipLearning.interfaces;

import gossipLearning.utils.SparseVector;



/**
 * Such a kind of model that can return the distribution of the class labels 
 * for the predictable instance.
 * @author István Hegedűs
 *
 */
public abstract class ProbabilityModel implements Model {
  private static final long serialVersionUID = -7154362879969974691L;

  public abstract Object clone();
  
  /**
   * Returns the distribution of the class labels for the specified instance.<br/><br/>
   * <b>NOTE:</b> We do not expect the real distribution here. The only requirement is 
   * the index of the maximal value corresponds to the most likely class.
   * @param instance instance for computing distribution
   * @return array of distribution
   */
  public abstract double[] distributionForInstance(SparseVector instance);
  
  /**
   * The default implementation of predict is simply based on finding the most likely class.
   */
  @Override
  public final double predict(SparseVector instance) {
    int maxLabelIndex = -1;
    double maxValue = Double.NEGATIVE_INFINITY;
    double[] distribution = distributionForInstance(instance);
    for (int i = 0; i < getNumberOfClasses(); i++){
      if (distribution[i] > maxValue){
        maxValue = distribution[i];
        maxLabelIndex = i;
      }
    }
    return maxLabelIndex;
  }
}
