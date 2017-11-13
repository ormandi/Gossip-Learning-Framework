package gossipLearning.models.learning.multiclass;

import java.util.Arrays;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.InstanceHolder;
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
  protected final double lambda;
  
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
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public MultiLogReg(String prefix) {
    this(prefix, PAR_LAMBDA);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_LAMBDA learning rate configuration string
   */
  protected MultiLogReg(String prefix, String PAR_LAMBDA) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
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
      w = new SparseVector[numberOfClasses -1];
      for (int i = 0; i < numberOfClasses -1; i++) {
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
  protected MultiLogReg(double lambda, double age, int numberOfClasses, SparseVector[] w, double[] distribution, double[] v, double[] bias) {
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
  public void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    double[] distribution = distributionForInstance(instance);
    
    // update for each classes
    for (int i = 0; i < numberOfClasses -1; i++) {
      double cDelta = (label == i) ? 1.0 : 0.0;
      double err = cDelta - distribution[i];
      
      w[i].mul(1.0 - nu * lambda);
      w[i].add(instance, nu * err);
      bias[i] += nu * err;
    }
  }
  
  public void update(InstanceHolder instances) {
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    SparseVector[] gradients = new SparseVector[w.length];
    double[] biasg = new double[w.length];
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double[] distribution = distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses-1; j++) {
        double cDelta = (label == j) ? 1.0 : 0.0;
        double err = cDelta - distribution[j];
        if (i == 0) {
          gradients[j] = new SparseVector();
          biasg[j] = 0.0;
        }
        gradients[j].add(instance, nu * err);
        biasg[j] += nu * err;
      }
    }
    
    for (int i = 0; i < numberOfClasses-1; i++) {
      w[i].mul(1.0 - nu * lambda);
      w[i].add(gradients[i], 1.0 / instances.size());
      bias[i] += biasg[i] / instances.size();
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
      w = new SparseVector[numberOfClasses -1];
      bias = new double[numberOfClasses -1];
      for (int i = 0; i < numberOfClasses -1; i++) {
        w[i] = new SparseVector();
      }
    }
  }

}
