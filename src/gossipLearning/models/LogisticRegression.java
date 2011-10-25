package gossipLearning.models;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;

/**
 * This class represents the logistic regression classifier. Using a Map<Integer,Double> 
 * collection as hyperplane and the 0th element of the collection represents the bias. 
 * This code is based on the Machine Learning book from Tom M. Mitchell.
 * @author István Hegedűs
 *
 */
public class LogisticRegression implements Model, SimilarityComputable<LogisticRegression> {
  private static final long serialVersionUID = -6445114719685631031L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda = 0.0001;
  
  /** @hidden */
  protected Map<Integer, Double> w;
  protected double age;
  protected int numberOfClasses = 2;
  
  /**
   * Initializes the hyperplane as 0 vector.
   */
  public LogisticRegression(){
    this.w = new TreeMap<Integer, Double>();
    this.age = 0.0;
  }
  
  /**
   * Returns a new logistic regression object that initializes its variable with 
   * the deep copy of the specified parameters.
   * @param w hyperplane
   * @param age model age
   * @param lambda learning parameter
   */
  protected LogisticRegression(Map<Integer, Double> w, double age, double lambda){
    this.w = new TreeMap<Integer, Double>();
    for (int k : w.keySet()){
      this.w.put(k, (double)w.get(k));
    }
    this.age = age;
    this.lambda = lambda;
  }
  
  /**
   * Clones the object.
   */
  public Object clone(){
    return new LogisticRegression(w, age, lambda);
  }

  @Override
  public void init(String prefix) {
    w = new TreeMap<Integer, Double>();
    age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    double prob = getPositiveProbability(instance);
    double err = label - prob;
    age ++;
    double nu = 1.0 / (lambda * age);
    int max = Utils.findMaxIdx(w, instance);
    for (int i = 0; i <= max; i ++) {
      Double wOldCompD = w.get(i);
      Double xCompD = instance.get(i);
      // using w0 as bias
      if (i == 0) {
        xCompD = 1.0;
      }
      if (wOldCompD != null || xCompD != null) {
        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
        w.put(i, (1.0 - nu * lambda) * wOldComp - nu * err * xComp);
      }
    }
  }
  
  /**
   * Computes the probability that the specified instance belongs to the positive class i.e. 
   * P(Y=1 | X=x, w) = 1 / (1 + e^(w'x + b)).
   * @param instance instance to compute the probability
   * @return positive label probability of the instance
   */
  private double getPositiveProbability(Map<Integer, Double> instance){
    double b = 0.0;
    if (w.containsKey(0)){
      b = w.get(0);
    }
    double predict = Utils.innerProduct(w, instance) + b;
    predict = Math.exp(predict) + 1.0;
    return 1.0 / predict;
  }

  /**
   * The label is negative when the inner product of the hyperplane and the 
   * specified instance plus the bias is greater than 0 <br/>
   * and positive otherwise, i.e. 1 < P(Y=0 | X=x) / P(Y=1 | X=x).
   */
  @Override
  public double predict(Map<Integer, Double> instance) {
    double b = 0.0;
    if (w.containsKey(0)){
      b = w.get(0);
    }
    double predict = Utils.innerProduct(w, instance) + b;
    return 0 <= predict ? 0.0 : 1.0;
  }
  
  @Override
  public double computeSimilarity(LogisticRegression model) {
    return Utils.computeSimilarity(w, model.w);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses != 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    this.numberOfClasses = numberOfClasses;
  }
  
  

}
