package gossipLearning.models.regression;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This class represents the linear regression learner.
 * @author István Hegedűs
 *
 */
public class LinearRegression implements Model, Mergeable<LinearRegression>, SimilarityComputable<LinearRegression> {
  private static final long serialVersionUID = -1468280308189482885L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  protected static final String PAR_LAMBDA = "LinearRegression.lambda";
  protected double lambda = 0.0001;
  
  /** @hidden */
  private SparseVector w;
  private double bias;
  private double age;
  
  private int numberOfClasses;
  
  /**
   * Creates a default model with age=0 and the regression hyperplane is the 0 vector.
   */
  public LinearRegression(){
    w = new SparseVector();
    age = 0.0;
    bias = 0.0;
  }
  
  private LinearRegression(SparseVector w, double age, double lambda, int numberOfClasses, double bias){
    this.w = (SparseVector)w.clone();
    this.age = age;
    this.lambda = lambda;
    this.numberOfClasses = numberOfClasses;
    this.bias = bias;
  }
  
  public Object clone(){
    return new LinearRegression(w, age, lambda, numberOfClasses, bias);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    w = new SparseVector();
    age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }

  @Override
  public void update(SparseVector instance, double label) {
    //SparseVector inst = SparseVector(instance);
    //inst.normalize();
    age ++;
    double err = label - predict(instance);
    
    //double nu = (1.0 / age) * 0.00001;
    double nu = 1.0 / (lambda * age);
    w.mul(1.0 - nu * lambda);
    w.add(instance, nu * err);
    bias += nu * err;
  }

  /**
   * In case of linear regression the prediction is w*x + b.
   */
  @Override
  public double predict(SparseVector instance) {
    return w.mul(instance) + bias;
  }

  @Override
  public double computeSimilarity(LinearRegression model) {
    return w.cosSim(model.w);
  }

  @Override
  public LinearRegression merge(LinearRegression model) {
    SparseVector mergedw = new SparseVector(w);
    double age = Math.round((this.age + model.age) / 2.0);
    double bias = (this.bias + model.bias) / 2.0;
    mergedw.mul(0.5);
    mergedw.add(model.w, 0.5);
    return new LinearRegression(mergedw, age, lambda, numberOfClasses, bias);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }
  
  public double getLambda() {
    return lambda;
  }

  public void setLambda(double lambda) {
    this.lambda = lambda;
  }

  @Override
  public String toString() {
    return w.toString() + "\t" + bias;
  }

}
