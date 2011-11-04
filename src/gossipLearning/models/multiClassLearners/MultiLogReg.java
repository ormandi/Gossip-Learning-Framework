package gossipLearning.models.multiClassLearners;

import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.utils.Utils;

public class MultiLogReg implements Model, ProbabilityModel {
  private static final long serialVersionUID = -3918448404565337980L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  private static final String PAR_LAMBDA = "lambda";
  private double lambda = 0.0001;
  
  private Map<Integer, Double>[] w;
  private double age;
  private int numberOfClasses = 2;

  /**
   * Constructs a default multi-class logistic regression. <br/>
   * NOTE: 
   */
  public MultiLogReg() {
    w = null;
    age = 0.0;
  }
  
  /**
   * Constructs a new multi-class logistic regression which is a deep copy of the 
   * specified object.
   * @param a to copy
   */
  @SuppressWarnings("unchecked")
  public MultiLogReg(MultiLogReg a) {
    lambda = a.lambda;
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    if (a.w == null) {
      w = null;
    } else {
      w = new Map[numberOfClasses];
      for (int i = 0; i < numberOfClasses; i++) {
        w[i] = new TreeMap<Integer, Double>();
        for (int key : a.w[i].keySet()) {
          w[i].put(key, a.w[i].get(key).doubleValue());
        }
      }
    }
  }
  
  /**
   * Deep copy.
   */
  public Object clone() {
    return new MultiLogReg(this);
  }

  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    double[] distribution = new double[numberOfClasses];
    double sum = 0.0;
    for (int i = 0; i < numberOfClasses -1; i++) {
      double bias = 0.0;
      if (w[i].containsKey(-1)) {
        bias = w[i].get(-1);
      }
      distribution[i] = Math.exp(bias + Utils.innerProduct(w[i], instance));
      sum += distribution[i];
    }
    distribution[numberOfClasses - 1] = 1.0;
    for (int i = 0; i < numberOfClasses; i++) {
      distribution[i] /= 1.0 + sum;
    }
    return distribution;
  }

  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    double[] distribution = distributionForInstance(instance);
    
    // update for each classes
    for (int j = 0; j < numberOfClasses; j++) {
      int max = Utils.findMaxIdx(w[j], instance);
      double cDelta = (label == j) ? 1.0 : 0.0;
      double err = cDelta - distribution[j];
      
      for (int i = -1; i <= max; i ++) {
        Double wOldCompD = w[j].get(i);
        Double xCompD = instance.get(i);
        // using w0 as bias
        if (i == -1) {
          xCompD = 1.0;
        }
        if (wOldCompD != null || xCompD != null) {
          double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
          double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
          w[i].put(i, (1.0 - nu * lambda) * wOldComp - nu * err * xComp);
        }
      }
    }
  }

  @Override
  public double predict(Map<Integer, Double> instance) {
    double[] distribution = distributionForInstance(instance);
    int maxIndex = -1;
    double maxValue = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < numberOfClasses; i++) {
      if (distribution[i] > maxValue) {
        maxValue = distribution[i];
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses < 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    this.numberOfClasses = numberOfClasses;
    w = new Map[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      w[i] = new TreeMap<Integer, Double>();
    }
  }

}
