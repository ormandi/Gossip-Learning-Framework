package gossipLearning.interfaces.models;

import gossipLearning.utils.SparseVector;

import java.util.Arrays;

/**
 * This class describes a weak learner for boosting algorithms. The weak learners 
 * have to candle multiple labeled classification tasks.
 * @author István Hegedűs
 *
 */
public abstract class WeakLearner extends ProbabilityModel{
  private static final long serialVersionUID = 1349841351687L;
  /**
   * Represents the weight of the current weak learner.
   */
  protected double alpha = 1.0;
  
  /**
   * Returns the weight of the current weak learner.
   * @return weight of the current weak learner
   */
  public double getAlpha() {
    return alpha;
  }
  
  /**
   * Sets the weight of the current weak learner to the specified value.
   * @param alpha weight to set
   */
  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }
  
  public abstract Object clone();
  
  /**
   * Calls the weighted version of update function with initial and uniform weights.
   */
  public final void update(final SparseVector instance, final double label){
    double[] uniformWeights = new double[getNumberOfClasses()];
    Arrays.fill(uniformWeights, 1.0 / (double)getNumberOfClasses());
    update(instance, label, uniformWeights);
  }
  
  /**
   * Updates the current model respect to the specified parameters.
   * @param instance training instance
   * @param label label of training instance
   * @param weight weights of possible class labels
   */
  public abstract void update(final SparseVector instance, final double label, final double[] weight);
}
