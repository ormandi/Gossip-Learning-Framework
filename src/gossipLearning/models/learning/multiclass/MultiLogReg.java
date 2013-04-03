package gossipLearning.models.learning.multiclass;

import java.util.Arrays;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

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
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "MultiLogReg.lambda";
  /**
   * Learning parameter.
   */
  protected double lambda = 0.0001;
  
  /**
   * The hyperplanes of the model.
   * @hidden
   */
  protected SparseVector[] w;
  /**
   * The biases of the model.
   */
  protected double[] bias;
  protected double[] distribution;
  protected double[] v;
  /**
   * The number of classes of the current classification problem.
   */
  protected int numberOfClasses = 0;

  /**
   * Constructs a default multi-class logistic regression. <br/>
   * NOTE: It works only after calling init(String prefix) and 
   * setNumberOfClasses(int numberOfClasses) functions.
   */
  public MultiLogReg() {
    w = null;
    age = 0.0;
  }
  
  /**
   * Constructs a new multi-class logistic regression which is a deep copy of the 
   * specified object.
   * @param a to copy
   */
  public MultiLogReg(MultiLogReg a) {
    lambda = a.lambda;
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    if (a.w == null) {
      w = null;
      bias = null;
    } else {
      w = new SparseVector[numberOfClasses];
      for (int i = 0; i < numberOfClasses; i++) {
        w[i] = (SparseVector)a.w[i].clone();
      }
      distribution = Arrays.copyOf(a.distribution, a.numberOfClasses);
      v = Arrays.copyOf(a.v, a.numberOfClasses);
      bias = Arrays.copyOf(a.bias, a.bias.length);
    }
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param lambda learning parameter
   * @param age number of updates
   * @param numberOfClasses number of classes
   * @param w array of hyperplanes
   * @param distribution template variable for the class distribution
   * @param v template variable for the class distribution
   * @param bias array of biases
   */
  protected MultiLogReg(double lambda, double age, int numberOfClasses, 
      SparseVector[] w, double[] distribution, double[] v, double[] bias) {
    this.lambda = lambda;
    this.age = age;
    this.numberOfClasses = numberOfClasses;
    this.w = w;
    this.distribution = distribution;
    this.v = v;
    this.bias = bias;
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
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    double[] distribution = distributionForInstance(instance);
    
    // update for each classes
    for (int j = 0; j < numberOfClasses; j++) {
      double cDelta = (label == j) ? 1.0 : 0.0;
      double err = cDelta - distribution[j];
      
      w[j].mul(1.0 - nu * lambda);
      w[j].add(instance, nu * err);
      bias[j] += nu * lambda * err;
    }
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses < 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    if (this.numberOfClasses != numberOfClasses) {
      this.numberOfClasses = numberOfClasses;
      distribution = new double[numberOfClasses];
      v = new double[numberOfClasses];
      w = new SparseVector[numberOfClasses];
      bias = new double[numberOfClasses];
      for (int i = 0; i < numberOfClasses; i++) {
        w[i] = new SparseVector();
      }
    }
  }

}
