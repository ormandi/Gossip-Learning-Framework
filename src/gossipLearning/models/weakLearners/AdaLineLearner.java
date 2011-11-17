package gossipLearning.models.weakLearners;

import gossipLearning.interfaces.WeakLearner;
import gossipLearning.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class AdaLineLearner extends WeakLearner {
  private static final long serialVersionUID = -1540156152482197419L;
  
  private static final String PAR_LAMBDA = "lambda";
  
  private Map<Integer, Double> w;
  private double[] v;
  private int numberOfClasses;
  private double age;
  private double lambda;

  public AdaLineLearner() {
    w = new TreeMap<Integer, Double>();
    age = 0.0;
  }
  
  public AdaLineLearner(AdaLineLearner a) {
    numberOfClasses = a.numberOfClasses;
    age = a.age;
    lambda = a.lambda;
    w = new TreeMap<Integer, Double>();
    if (a.w != null) {
      for (int key : a.w.keySet()) {
        w.put(key, a.w.get(key).doubleValue());
      }
    }
    v = new double[numberOfClasses];
    if (a.v != null) {
      for (int i = 0; i < numberOfClasses; i++) {
        v[i] = a.v[i];
      }
    }
  }
  
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
    v = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      v[i] = CommonState.r.nextBoolean() ? 1.0 : -1.0;
    }
  }

  @Override
  public Object clone() {
    return new AdaLineLearner(this);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label, double[] weight) {
    age ++;
    double nu = 1.0 / (double) (age * lambda); // regularized
    double innerProd = Utils.innerProduct(w, instance);
    double[] distribution = distributionForInstance(instance);
    double yl;
    double exp;
    
    // update w
    for (int key : w.keySet()) {
      if (instance.containsKey(key)) {
        double value = w.get(key).doubleValue();
        double x = instance.get(key).doubleValue();
        double sum = 0;
        for (int l = 0; l < numberOfClasses; l++) {
          yl = (label == l) ? 1.0 : -1.0;
          exp = Math.exp(-yl * distribution[l]);
          sum -= weight[l] * exp * yl * v[l] * x;
        }
        w.put(key, (1.0 - 1.0/age) * value - nu * sum);
      }
    }
    for (int key : instance.keySet()) {
      if (!w.containsKey(key)) {
        double x = instance.get(key).doubleValue();
        double sum = 0;
        for (int l = 0; l < numberOfClasses; l++) {
          yl = (label == l) ? 1.0 : -1.0;
          exp = Math.exp(-yl * distribution[l]);
          sum -= weight[l] * exp * yl * v[l] * x;
        }
        w.put(key, - nu * sum);
      }
    }
    
    // update v
    for (int l = 0; l < numberOfClasses; l++) {
      yl = (label == l) ? 1.0 : -1.0;
      exp = Math.exp(-yl * distribution[l]);
      v[l] += (1.0/age) * weight[l] * exp * yl * innerProd;
    }
  }

  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    double[] distribution = new double[numberOfClasses];
    double innerProd = Utils.innerProduct(w, instance);
    for (int i = 0; i < numberOfClasses; i++) {
      distribution[i] = v[i] * innerProd;
    }
    return Utils.normalize(distribution);
  }

}
