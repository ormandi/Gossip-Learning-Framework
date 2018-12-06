package gossipLearning.models.learning;

import gossipLearning.interfaces.models.LinearModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

/**
 * This class represents the logistic regression classifier. Using a Map<Integer,Double> 
 * collection as hyperplane and the 0th element of the collection represents the bias. 
 * This code is based on the Machine Learning book from Tom M. Mitchell.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>LogisticRegression.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class LogisticRegression extends LinearModel {
  private static final long serialVersionUID = -6445114719685631031L;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public LogisticRegression(String prefix){
    super(prefix);
  }
  
  public LogisticRegression(double lambda) {
    super(lambda);
  }
  
  /**
   * Returns a new logistic regression object that initializes its variable with 
   * the deep copy of the specified parameter.
   * @param learner to be cloned
   */
  protected LogisticRegression(LogisticRegression a){
    super(a);
  }
  
  /**
   * Clones the object.
   */
  public Object clone(){
    return new LogisticRegression(this);
  }

  protected void gradient(SparseVector instance, double label) {
    double prob = getPositiveProbability(instance);
    double err = label - prob;
    gradient.set(w).mul(lambda).add(instance, err);
    biasGradient = err;
  }
  
  protected void gradient(InstanceHolder instances) {
    gradient.set(w).mul(lambda * instances.size());
    biasGradient = 0.0;
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double prob = getPositiveProbability(instance);
      double err = label - prob;
      gradient.add(instance, err);
      biasGradient += err;
    }
  }
  
  /**
   * Computes the probability that the specified instance belongs to the positive class i.e. 
   * P(Y=1 | X=x, w) = 1 / (1 + e^(w'x + b)).
   * @param instance instance to compute the probability
   * @return positive label probability of the instance
   */
  protected double getPositiveProbability(SparseVector instance){
    double predict = w.mul(instance) + bias;
    predict = Math.exp(predict) + 1.0;
    return 1.0 / predict;
  }
  
  public double[] distributionForInstance(SparseVector instance) {
    distribution[1] = getPositiveProbability(instance);
    distribution[0] = 1.0 - distribution[1];
    return distribution;
  }

}
