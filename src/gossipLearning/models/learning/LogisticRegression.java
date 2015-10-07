package gossipLearning.models.learning;

import java.util.Arrays;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.SimilarityComputable;
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
  private static final String PAR_ALPHA = "LogisticRegression.alpha";
  protected final double lambda;
  protected final double alpha;
  
  /** @hidden */
  protected SparseVector w;
  protected double bias;
  protected double[] distribution;
  protected int numberOfClasses = 2;
  protected double apr_err;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public LogisticRegression(String prefix){
    this(prefix, PAR_LAMBDA, PAR_ALPHA);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_LAMBDA learning rate configuration string
   */
  protected LogisticRegression(String prefix, String PAR_LAMBDA, String PAR_ALPHA) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
    w = new SparseVector();
    bias = 0.0;
    distribution = new double[numberOfClasses];
    age = 0.0;
    apr_err = 0.0;
  }
  
  /**
   * Returns a new logistic regression object that initializes its variable with 
   * the deep copy of the specified parameter.
   * @param learner to be cloned
   */
  protected LogisticRegression(LogisticRegression a){
    lambda = a.lambda;
    alpha = a.alpha;
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    distribution = Arrays.copyOf(a.distribution, a.numberOfClasses);
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    apr_err = a.apr_err;
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param lambda learning parameter
   * @param w hyperplane
   * @param bias bias variable
   * @param distribution template variable for the class distribution
   * @param age number of updates
   * @param numberOfClasses number of classes
   */
  protected LogisticRegression(double lambda, SparseVector w, double bias, double[] distribution, double age, int numberOfClasses, double approxError, double alpha) {
    this.lambda = lambda;
    this.w = w;
    this.bias = bias;
    this.distribution = distribution;
    this.age = age;
    this.numberOfClasses = numberOfClasses;
    this.apr_err = approxError;
    this.alpha = alpha;
  }
  
  /**
   * Clones the object.
   */
  public Object clone(){
    return new LogisticRegression(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    double prob = getPositiveProbability(instance);
    double err = label - prob;
    age ++;
    double nu = 1.0 / (1 + (lambda * age));
    
    w.mul(1.0 - nu * lambda);
    w.add(instance, - nu * err);
    bias -= nu * lambda * err;
    
    err = Math.abs(label - Math.round(prob));
    apr_err *= 1.0 - alpha; 
    apr_err += alpha * err;
  }
  
  /**
   * Computes the probability that the specified instance belongs to the positive class i.e. 
   * P(Y=1 | X=x, w) = 1 / (1 + e^(w'x + b)).
   * @param instance instance to compute the probability
   * @return positive label probability of the instance
   */
  public double getPositiveProbability(SparseVector instance){
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
  public int getNumberOfClasses() {
    return numberOfClasses;
  }
  
  public double getApproximatedError(){
    return apr_err;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses != 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    this.numberOfClasses = numberOfClasses;
  }
  
  @Override
  public String toString() {
    return bias + "\t" + w.toString();
  }

}