package gossipLearning.interfaces.models;

import java.util.Arrays;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;



/**
 * Such a kind of model that can return the distribution of the class labels 
 * for the predictable instance.
 * 
 * @author István Hegedűs
 */
public abstract class ProbabilityModel implements LearningModel {
  private static final long serialVersionUID = -7154362879969974691L;
  protected double age;
  protected int numberOfClasses;
  protected int numberOfFeatures;
  protected double[] distribution;
  
  public ProbabilityModel() {
    age = 0.0;
    numberOfClasses = 0;
    numberOfFeatures = 0;
    distribution = null;
  }
  
  public ProbabilityModel(ProbabilityModel a) {
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    numberOfFeatures = a.numberOfFeatures;
    if (a.distribution != null) {
      distribution = Arrays.copyOf(a.distribution, a.numberOfClasses);
    }
  }
  
  @Override
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
    int maxLabelIndex = 0;
    double maxValue = Double.NEGATIVE_INFINITY;
    double[] distribution = distributionForInstance(instance);
    for (int i = 0; i < numberOfClasses; i++){
      if (distribution[i] > maxValue){
        maxValue = distribution[i];
        maxLabelIndex = i;
      }
    }
    return maxLabelIndex;
  }
  
  public void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }
  
  @Override
  public final double getAge() {
    return age;
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    this.numberOfClasses = numberOfClasses;
    this.numberOfFeatures = numberOfFeatures;
    distribution = new double[numberOfClasses];
  }
}
