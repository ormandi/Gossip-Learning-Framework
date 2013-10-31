package gossipLearning.models.learning;

import java.util.Arrays;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.functions.Step;
import gossipLearning.interfaces.functions.ConstantGradient;
import gossipLearning.interfaces.functions.Sigmoid;
import gossipLearning.interfaces.functions.SigmoidGradient;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class Perceptron extends ProbabilityModel {
  private static final long serialVersionUID = -817025202609991782L;
  protected static final String PAR_LAMBDA = "Perceptron.lambda";
  protected static final String PAR_FUNC = "Perceptron.usingSigmoid";
  
  protected double lambda;
  protected boolean usingSigmoid;
  protected int numberOfClasses;
  protected double[] distribution;
  
  protected SparseVector w;
  protected double bias;
  
  protected Function fAct;
  protected Function fGrad;
  
  public Perceptron() {
    age = 0.0;
    distribution = new double[2];
    w = new SparseVector();
    bias = 0.0;
    fAct = new Sigmoid();
    fGrad = new SigmoidGradient();
  }
  
  public Perceptron(Perceptron a) {
    age = a.age;
    lambda = a.lambda;
    usingSigmoid = a.usingSigmoid;
    numberOfClasses = a.numberOfClasses;
    distribution = Arrays.copyOf(a.distribution, a.distribution.length);
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    if (usingSigmoid) {
      fAct = new Sigmoid();
      fGrad = new SigmoidGradient();
    } else {
      fAct = new Step();
      fGrad = new ConstantGradient();
    }
  }
  
  protected Perceptron(double age, double lambda, boolean usingSigmoid, int numberOfClasses, 
      double[] distribution, SparseVector w, double bias) {
    this.age = age;
    this.lambda = lambda;
    this.usingSigmoid = usingSigmoid;
    this.numberOfClasses = numberOfClasses;
    this.distribution = distribution;
    this.w = w;
    this.bias = bias;
    if (usingSigmoid) {
      fAct = new Sigmoid();
      fGrad = new SigmoidGradient();
    } else {
      fAct = new Step();
      fGrad = new ConstantGradient();
    }
  }

  @Override
  public Object clone() {
    return new Perceptron(this);
  }

  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    usingSigmoid = Configuration.getBoolean(prefix + "." + PAR_FUNC);
    if (!usingSigmoid) {
      fAct = new Step();
      fGrad = new ConstantGradient();
    }
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
