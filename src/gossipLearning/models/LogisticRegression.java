package gossipLearning.models;

import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;

import gossipLearning.interfaces.Mergable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.Utils;

/**
 * This class represents the logistic regression classifier.
 * @author István Hegedűs
 *
 */
public class LogisticRegression implements Model, Mergable<LogisticRegression>, SimilarityComputable<LogisticRegression> {
  private static final long serialVersionUID = -6445114719685631031L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda = 0.0001;
  
  /** @hidden */
  private Map<Integer, Double> w;
  private double age;
  
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
  private LogisticRegression(Map<Integer, Double> w, double age, double lambda){
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
    if (label == -1.0) {
      label = 0.0;
    }
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
        w.put(i, (1.0 - 1.0 / age) * wOldComp - nu * err * xComp);
      }
    }
  }
  
  private double getPositiveProbability(Map<Integer, Double> instance){
    double b = 0.0;
    if (w.containsKey(0)){
      b = w.get(0);
    }
    double predict = Utils.innerProduct(w, instance) + b;
    predict = Math.exp(predict) + 1.0;
    return 1.0 / predict;
  }

  @Override
  public double predict(Map<Integer, Double> instance) {
    double b = 0.0;
    if (w.containsKey(0)){
      b = w.get(0);
    }
    double predict = Utils.innerProduct(w, instance) + b;
    return 0 < predict ? -1.0 : 1.0;
  }
  
  @Override
  public double computeSimilarity(LogisticRegression model) {
    return Utils.computeSimilarity(w, model.w);
  }

  @Override
  public LogisticRegression merge(LogisticRegression model) {
    Map<Integer, Double> mergedw = new TreeMap<Integer, Double>();
    double age = Math.round((this.age + model.age) / 2.0);
    double value;
    for (int i : w.keySet()){
      value = w.get(i);
      if (model.w.containsKey(i)){
        value += model.w.get(i);
      }
      value /= 2.0;
      mergedw.put(i, value);
    }
    for (int i : model.w.keySet()){
      if (!w.containsKey(i)){
        mergedw.put(i, model.w.get(i) / 2.0);
      }
    }
    return new LogisticRegression(mergedw, age, lambda);
  }

}
