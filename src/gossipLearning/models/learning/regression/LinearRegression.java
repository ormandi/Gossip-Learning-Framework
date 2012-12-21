package gossipLearning.models.learning.regression;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This class represents the linear regression learner.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>LinearRegression.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class LinearRegression implements LearningModel, Mergeable<LinearRegression>, SimilarityComputable<LinearRegression> {
  private static final long serialVersionUID = -1468280308189482885L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "LinearRegression.lambda";
  protected double lambda;
  
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
    bias = 0.0;
    age = 0.0;
  }
  
  private LinearRegression(LinearRegression a){
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    age = a.age;
    lambda = a.lambda;
    numberOfClasses = a.numberOfClasses;
  }
  
  public Object clone(){
    return new LinearRegression(this);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double err = label - predict(instance);
    
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
    age = Math.max(age, model.age);
    bias = (bias + model.bias) / 2.0;
    w.mul(0.5);
    w.add(model.w, 0.5);
    return this;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public String toString() {
    return w.toString() + "\t" + bias;
  }

}
