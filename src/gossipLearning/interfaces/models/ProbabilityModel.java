package gossipLearning.interfaces.models;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

import peersim.config.Configuration;



/**
 * Such a kind of model that can return the distribution of the class labels 
 * for the predictable instance.
 * 
 * @author István Hegedűs
 */
public abstract class ProbabilityModel implements LearningModel {
  private static final long serialVersionUID = -7154362879969974691L;
  private static final String PAR_LAMBDA = "lambda";
  
  protected final double lambda;
  protected double age;
  protected int numberOfClasses;
  protected int numberOfFeatures;
  protected double[] distribution;
  
  public ProbabilityModel(double lambda) {
    age = 0.0;
    numberOfClasses = 0;
    numberOfFeatures = 0;
    distribution = null;
    this.lambda = lambda;
  }
  
  public ProbabilityModel(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    age = 0.0;
    numberOfClasses = 0;
    numberOfFeatures = 0;
    distribution = null;
  }
  
  public ProbabilityModel(ProbabilityModel a) {
    lambda = a.lambda;
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
  
  @Override
  public void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    if (1 < instances.size()) {
      throw new RuntimeException("Batch update is not implemented for this learner!");
    }
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }
  
  @Override
  public final void update(InstanceHolder instances, int epoch, int batchSize) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    if (batchSize == 0 || instances.size() <= batchSize) {
      // full batch update
      for (int e = 0; e < epoch; e++) {
        update(instances);
      }
    } else {
      // mini-batch/SGD update
      InstanceHolder batch = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
      for (int e = 0; e < epoch; e++) {
        for (int i = 0; i < instances.size(); i++) {
          batch.add(instances.getInstance(i), instances.getLabel(i));
          if (batch.size() == batchSize) {
            update(batch);
            batch.clear();
          }
        }
      }
      if (0 < batch.size()) {
        update(batch);
        batch.clear();
      }
    }
  }
  
  @Override
  public final double getAge() {
    return age;
  }
  
  @Override
  public final void setAge(double age) {
    this.age = age;
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    this.numberOfClasses = numberOfClasses;
    this.numberOfFeatures = numberOfFeatures;
    distribution = new double[numberOfClasses];
  }
  
  @Override
  public void clear() {
    age = 0.0;
    for (int i = 0; i < numberOfClasses; i++) {
      distribution[i] = 0.0;
    }
  }
  
  @Override
  public String toString() {
    return "age: " + age;
  }
}
