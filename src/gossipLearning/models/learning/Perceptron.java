package gossipLearning.models.learning;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

import peersim.config.Configuration;

public class Perceptron extends ProbabilityModel {
  private static final long serialVersionUID = -817025202609991782L;
  protected static final String PAR_LAMBDA = "Perceptron.lambda";
  protected static final String PAR_AFUNC = "Perceptron.activation";
  protected static final String PAR_GFUNC = "Perceptron.gradient";
  
  protected final double lambda;
  protected int numberOfClasses;
  protected double[] distribution;
  
  protected SparseVector w;
  protected double bias;
  
  protected final Function fAct;
  protected final Function fGrad;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public Perceptron(String prefix) {
    this(prefix, PAR_LAMBDA, PAR_AFUNC, PAR_GFUNC);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_LAMBDA learning rate configuration string
   * @param PAR_AFUNC activation function configuration string
   * @param PAR_GFUNC gradient function configuration string
   */
  public Perceptron(String prefix, String PAR_LAMBDA, String PAR_AFUNC, String PAR_GFUNC) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    try {
      fAct = (Function)Class.forName(Configuration.getString(prefix + "." + PAR_AFUNC)).newInstance();
      fGrad = (Function)Class.forName(Configuration.getString(prefix + "." + PAR_GFUNC)).newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Can not create function. ", e);
    }
    age = 0.0;
    distribution = new double[2];
    w = new SparseVector();
    bias = 0.0;
  }
  
  public Perceptron(double age, double lambda, Function fAct, Function fGrad, int numberOfClasses, double[] distribution, SparseVector w, double bias) {
    this.age = age;
    this.lambda = lambda;
    this.fAct = fAct;
    this.fGrad = fGrad;
    this.numberOfClasses = numberOfClasses;
    this.distribution = distribution;
    this.w = w;
    this.bias = bias;
  }
  
  public Perceptron(Perceptron a) {
    age = a.age;
    lambda = a.lambda;
    numberOfClasses = a.numberOfClasses;
    distribution = Arrays.copyOf(a.distribution, a.distribution.length);
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    fAct = a.fAct;
    fGrad = a.fGrad;
  }
  
  @Override
  public Object clone() {
    return new Perceptron(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    
    double product = w.mul(instance) + bias;
    double grad = (fAct.execute(product) - label) * fGrad.execute(product);
    w.mul(1.0 - nu * lambda);
    w.add(instance, - nu * grad);
    bias -= nu * lambda * grad;
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double activation = fAct.execute(w.mul(instance) + bias);
    distribution[0] = 1.0 - activation;
    distribution[1] = activation;
    return distribution;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses != 2) {
      throw new IllegalArgumentException("This class handles only 2 classes instead: " + numberOfClasses);
    }
    this.numberOfClasses = numberOfClasses;
  }

}
