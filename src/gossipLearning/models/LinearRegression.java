package gossipLearning.models;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

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
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda = 0.0001;
  
  /** @hidden */
  private Map<Integer, Double> w;
  private double age;
  
  private int numberOfClasses;
  
  /**
   * Creates a default model with age=0 and the regression hyperplane is the 0 vector.
   */
  public LinearRegression(){
    w = new TreeMap<Integer, Double>();
    age = 0.0;
  }
  
  private LinearRegression(Map<Integer, Double> w, double age, double lambda, int numberOfClasses){
    this.w = new TreeMap<Integer, Double>();
    for (int k : w.keySet()){
      this.w.put(k, (double)w.get(k));
    }
    this.age = age;
    this.lambda = lambda;
    this.numberOfClasses = numberOfClasses;
  }
  
  public Object clone(){
    return new LinearRegression(w, age, lambda, numberOfClasses);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    w = new TreeMap<Integer, Double>();
    age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    double err = label - Utils.innerProduct(w, instance);
    age ++;
    double nu = 1.0 / (lambda * age);
    int max = Utils.findMaxIdx(w, instance);
    for (int i = -1; i <= max; i ++) {
      Double wOldCompD = w.get(i);
      Double xCompD = instance.get(i);
      // using w0 as bias
      if (i == -1) {
        xCompD = 1.0;
      }
      if (wOldCompD != null || xCompD != null) {
        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
        w.put(i, (1.0 - 1.0 / age) * wOldComp - nu * err * xComp);
      }
    }
  }

  /**
   * In case of linear regression the prediction is w*x + b.
   */
  @Override
  public double predict(Map<Integer, Double> instance) {
    double b = 0.0;
    if (w.containsKey(-1)) {
      b = w.get(-1);
    }
    return Utils.innerProduct(w, instance) + b;
  }

  @Override
  public double computeSimilarity(LinearRegression model) {
    return Utils.computeSimilarity(w, model.w);
  }

  @Override
  public LinearRegression merge(LinearRegression model) {
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
    return new LinearRegression(mergedw, age, lambda, numberOfClasses);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }

}
