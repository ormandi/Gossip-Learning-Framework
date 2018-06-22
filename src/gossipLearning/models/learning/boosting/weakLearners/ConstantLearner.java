package gossipLearning.models.learning.boosting.weakLearners;

import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.utils.SparseVector;

/**
 * This class represents a learner that predicts the most frequented class label.
 * @author István Hegedűs
 *
 */
public class ConstantLearner extends WeakLearner {
  private static final long serialVersionUID = -1090945222157723437L;
  
  /**
   * Constructs a default learner.
   */
  public ConstantLearner(String prefix, double lambda, long seed) {
    super(prefix, lambda, seed);
  }
  
  /**
   * Deep copy constructor.
   * @param a to copy
   */
  public ConstantLearner(ConstantLearner a) {
    super(a);
    this.alpha = a.alpha;
    if (a.distribution != null) {
      this.distribution = new double[numberOfClasses];
      for (int i = 0; i < numberOfClasses; i++) {
        this.distribution[i] = a.distribution[i];
      }
    }
  }

  @Override
  public Object clone() {
    return new ConstantLearner(this);
  }

  @Override
  public void update(SparseVector instance, double label, double[] weight) {
    age ++;
    for (int i = 0; i < numberOfClasses; i++) {
      if (i == label) {
        distribution[i] -= weight[i];
      } else {
        distribution[i] += weight[i];
      }
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    return distribution;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Constant[Alpha=" + alpha);
    sb.append("\tAge=" + age);
    sb.append("]");
    return sb.toString();
  }

}
