package gossipLearning.models.weakLearners;

import gossipLearning.interfaces.WeakLearner;
import gossipLearning.utils.SparseVector;

/**
 * This class represents a learner that predicts the most frequented class label.
 * @author István Hegedűs
 *
 */
public class ConstantLearner extends WeakLearner {
  private static final long serialVersionUID = -1090945222157723437L;
  
  private double[] distribution;
  private double age;
  private int numberOfClasses;
  
  /**
   * Constructs a default learner.
   */
  public ConstantLearner() {
    age = 0;
  }
  
  /**
   * Deep copy constructor.
   * @param a to copy
   */
  public ConstantLearner(ConstantLearner a) {
    this.age = a.age;
    this.alpha = a.alpha;
    this.numberOfClasses = a.numberOfClasses;
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
  public void init(String prefix) {
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

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
    distribution = new double[numberOfClasses];
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Constant[Alpha=" + alpha);
    sb.append("\tAge=" + age);
    sb.append("]");
    return sb.toString();
  }

}
