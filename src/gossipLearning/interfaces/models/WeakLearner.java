package gossipLearning.interfaces.models;

import gossipLearning.utils.InstanceHolder;
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
  protected final String prefix;
  /**
   * Represents the weight of the current weak learner.
   */
  protected double alpha = 1.0;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public WeakLearner(String prefix) {
    this.prefix = prefix; 
  }
  
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
    double[] uniformWeights = new double[numberOfClasses];
    Arrays.fill(uniformWeights, 1.0 / (double)numberOfClasses);
    update(instance, label, uniformWeights);
  }
  
  @Override
  public final void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }
  
  /**
   * Updates the current model respect to the specified parameters.
   * @param instance training instance
   * @param label label of training instance
   * @param weight weights of possible class labels
   */
  public abstract void update(final SparseVector instance, final double label, final double[] weight);
}
