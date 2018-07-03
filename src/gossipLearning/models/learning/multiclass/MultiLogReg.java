package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

/**
 * This class represents the multi-class logistic regression classifier. 
 * Uses Map<Integer,Double> collections to represent hyperplanes and the 
 * -1th element of the collections represents the bias. 
 * This code is based on the Machine Learning book from Tom M. Mitchell.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MultiLogReg.lambda - learning rate</li>
 * </ul>
 * 
 * @author István Hegedűs
 */
public class MultiLogReg extends ProbabilityModel {
  private static final long serialVersionUID = -3918448404565337980L;
  
  /**
   * The hyperplanes of the model.
   * @hidden
   */
  protected SparseVector[] w;
  protected SparseVector[] gradients;
  /**
   * The biases of the model.
   */
  protected double[] bias;
  protected double[] v;
  protected double[] biasGradients;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public MultiLogReg(String prefix) {
    super(prefix);
  }
  
  /**
   * Constructs a new multi-class logistic regression which is a deep copy of the 
   * specified object.
   * @param a to copy
   */
  public MultiLogReg(MultiLogReg a) {
    super(a);
    if (a.w != null) {
      w = new SparseVector[numberOfClasses -1];
      gradients = new SparseVector[numberOfClasses -1];
      for (int i = 0; i < numberOfClasses -1; i++) {
        w[i] = (SparseVector)a.w[i].clone();
        gradients[i] = (SparseVector)a.gradients[i].clone();
      }
      v = Arrays.copyOf(a.v, a.numberOfClasses);
      bias = Arrays.copyOf(a.bias, a.bias.length);
      biasGradients = Arrays.copyOf(a.biasGradients, a.biasGradients.length);
    }
  }
  
  /**
   * Deep copy.
   */
  public Object clone() {
    return new MultiLogReg(this);
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double sum = 0.0;
    for (int i = 0; i < numberOfClasses -1; i++) {
      v[i] = bias[i] + w[i].mul(instance);
    }
    v[numberOfClasses - 1] = 0.0;
    
    for (int i = 0; i < numberOfClasses; i++) {
      sum = 0.0;
      for (int j = 0; j < numberOfClasses -1; j++) {
        sum += Math.exp(v[j] - v[i]);
      }
      distribution[i] = 1.0 / (sum + Math.exp(-v[i]));
    }
    return distribution;
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    
    gradient(instance, label);
    for (int i = 0; i < w.length; i++) {
      w[i].add(gradients[i], - nu);
      bias[i] -= nu * biasGradients[i];
    }
  }
  
  protected void gradient(SparseVector instance, double label) {
    double[] distribution = distributionForInstance(instance);
    
    for (int i = 0; i < numberOfClasses -1; i++) {
      double cDelta = (label == i) ? 1.0 : 0.0;
      double err = cDelta - distribution[i];
      
      gradients[i].set(w[i]).mul(lambda).add(instance, -err);
      biasGradients[i] = lambda * -err;
    }
  }
  
  public void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    
    gradient(instances);
    for (int i = 0; i < numberOfClasses-1; i++) {
      w[i].add(gradients[i], - nu);
      bias[i] -= nu * biasGradients[i];
    }
  }
  
  protected void gradient(InstanceHolder instances) {
    for (int i = 0; i < w.length; i++) {
      gradients[i].set(w[i]).mul(lambda * instances.size());
      biasGradients[i] = 0.0;
    }
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double[] distribution = distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses-1; j++) {
        double cDelta = (label == j) ? 1.0 : 0.0;
        double err = cDelta - distribution[j];
        gradients[j].add(instance, -err);
        biasGradients[j] += lambda * -err;
      }
    }
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    v = new double[numberOfClasses];
    w = new SparseVector[numberOfClasses -1];
    gradients = new SparseVector[numberOfClasses -1];
    bias = new double[numberOfClasses -1];
    biasGradients = new double[numberOfClasses -1];
    for (int i = 0; i < numberOfClasses -1; i++) {
      w[i] = new SparseVector();
      gradients[i] = new SparseVector();
    }
  }

}
