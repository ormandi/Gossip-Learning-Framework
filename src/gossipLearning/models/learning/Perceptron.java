package gossipLearning.models.learning;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class Perceptron extends ProbabilityModel {
  private static final long serialVersionUID = -817025202609991782L;
  protected static final String PAR_LAMBDA = "Perceptron.lambda";
  protected static final String PAR_AFUNC = "Perceptron.activation";
  protected static final String PAR_GFUNC = "Perceptron.gradient";
  
  protected final double lambda;
  
  protected SparseVector w;
  protected double bias;
  protected SparseVector gradient;
  protected double biasGradient;
  
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
    w = new SparseVector();
    gradient = new SparseVector();
    bias = 0.0;
    biasGradient = 0.0;
  }
  
  public Perceptron(Perceptron a) {
    super(a);
    lambda = a.lambda;
    w = (SparseVector)a.w.clone();
    gradient = (SparseVector)a.gradient.clone();
    bias = a.bias;
    biasGradient = a.biasGradient;
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
    
    /*double product = w.mul(instance) + bias;
    double grad = (fAct.execute(product) - label) * fGrad.execute(product);
    w.mul(1.0 - nu * lambda);
    w.add(instance, - nu * grad);
    bias -= nu * lambda * grad;*/
    
    gradient(instance, label);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
  }
  
  @Override
  public void update(InstanceHolder instances) {
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    gradient(instances);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
  }
  
  protected void gradient(SparseVector instance, double label) {
    double product = w.mul(instance) + bias;
    double grad = (fAct.execute(product) - label) * fGrad.execute(product);
    gradient.set(w).mul(lambda).add(instance, grad);
    biasGradient = lambda * grad;
  }
  
  protected void gradient(InstanceHolder instances) {
    gradient.set(w).mul(lambda * instances.size());
    biasGradient = 0.0;
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double product = w.mul(instance) + bias;
      double grad = (fAct.execute(product) - label) * fGrad.execute(product);
      gradient.add(instance, grad);
      biasGradient += lambda * grad;
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double activation = fAct.execute(w.mul(instance) + bias);
    distribution[0] = 1.0 - activation;
    distribution[1] = activation;
    return distribution;
  }

}
