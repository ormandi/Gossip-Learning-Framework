package gossipLearning.models.learning;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

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
public class LogisticRegression extends ProbabilityModel implements SimilarityComputable<LogisticRegression> {
  private static final long serialVersionUID = -6445114719685631031L;
  
  /** @hidden */
  private static final String PAR_LAMBDA = "LogisticRegression.lambda";
  protected final double lambda;
  
  /** @hidden */
  protected SparseVector w;
  protected double bias;
  protected SparseVector gradient;
  protected double biasGradient;
  
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public LogisticRegression(String prefix){
    this(prefix, PAR_LAMBDA);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_LAMBDA learning rate configuration string
   */
  protected LogisticRegression(String prefix, String PAR_LAMBDA) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    w = new SparseVector();
    bias = 0.0;
    gradient = new SparseVector();
    biasGradient = 0.0;
  }
  
  /**
   * Returns a new logistic regression object that initializes its variable with 
   * the deep copy of the specified parameter.
   * @param learner to be cloned
   */
  protected LogisticRegression(LogisticRegression a){
    super(a);
    lambda = a.lambda;
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    gradient = (SparseVector)a.gradient.clone();
    biasGradient = a.biasGradient;
  }
  
  /**
   * Clones the object.
   */
  public Object clone(){
    return new LogisticRegression(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    
    gradient(instance, label);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
    
  }
  
  public void update(InstanceHolder instances) {
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    
    gradient(instances);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
  }
  
  protected void gradient(SparseVector instance, double label) {
    double prob = getPositiveProbability(instance);
    double err = label - prob;
    gradient.set(w).mul(lambda).add(instance, err);
    biasGradient = lambda * err;
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
      biasGradient += lambda * err;
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

  @Override
  public double computeSimilarity(LogisticRegression model) {
    return w.cosSim(model.w);
  }
  
  @Override
  public String toString() {
    return bias + "\t" + w.toString();
  }

}
