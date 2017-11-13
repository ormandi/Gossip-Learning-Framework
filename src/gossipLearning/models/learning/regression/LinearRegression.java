package gossipLearning.models.learning.regression;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Set;

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
public class LinearRegression implements LearningModel, Mergeable<LinearRegression>, Partializable<LinearRegression>, SimilarityComputable<LinearRegression> {
  private static final long serialVersionUID = -1468280308189482885L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "LinearRegression.lambda";
  protected final double lambda;
  
  /** @hidden */
  private SparseVector w;
  private double bias;
  private double age;
  
  private int numberOfClasses;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public LinearRegression(String prefix){
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    w = new SparseVector();
    bias = 0.0;
    age = 0.0;
  }
  
  protected LinearRegression(LinearRegression a){
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    age = a.age;
    lambda = a.lambda;
    numberOfClasses = a.numberOfClasses;
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param w hyperplane
   * @param bias bias variable
   * @param age number of updates
   * @param lambda learning parameter
   * @param numberOfClasses number of classes
   */
  protected LinearRegression(SparseVector w, double bias, double age, double lambda, int numberOfClasses) {
    this.w = w;
    this.bias = bias;
    this.age = age;
    this.lambda = lambda;
    this.numberOfClasses = numberOfClasses;
  }
  
  public Object clone(){
    return new LinearRegression(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double err = label - predict(instance);
    //System.out.println(label + "\t" + predict(instance));
    
    double nu = 1.0 / (lambda * age);
    w.mul(1.0 - nu * lambda);
    w.add(instance, nu * err);
    bias += nu * lambda * err;
  }
  
  public void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
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

  @Override
  public LinearRegression getModelPart(Set<Integer> indices) {
    SparseVector w = new SparseVector(indices.size());
    for (int index : indices) {
      w.add(index, this.w.get(index));
    }
    return new LinearRegression(w, bias, age, lambda, numberOfClasses);
  }

}
