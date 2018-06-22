package gossipLearning.models.gmm;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;

import java.util.Random;

public class GMM implements Model, Mergeable {
  private static final long serialVersionUID = 5724245405575938223L;
  
  protected GaussModel[] models;
  protected double[] coefs;
  protected double age;
  protected Random r;
  protected final int k;
  
  protected double[] probs;
  
  public GMM(int k) {
    this(k, new Random(System.nanoTime()));
  }
  
  public GMM(int k, Random r) {
    this.k = k;
    age = 0.0;
    models = new GaussModel[k];
    coefs = new double[k];
    probs = new double[k];
    for (int i = 0; i < k; i++) {
      models[i] = new GaussModel();
      coefs[i] = 1.0 / k;
    }
    this.r = r;
  }
  
  public GMM(GMM a) {
    k = a.k;
    models = new GaussModel[k];
    coefs = new double[k];
    probs = new double[k];
    age = a.age;
    r = a.r;
    for (int i = 0; i < k; i++) {
      models[i] = (GaussModel)a.models[i].clone();
      coefs[i] = a.coefs[i];
    }
  }
  
  @Override
  public Object clone() {
    return new GMM(this);
  }
  
  public void update(double x) {
    age ++;
    double sum = 0.0;
    for (int i = 0; i < k; i++) {
      probs[i] = 1.0 - 2.0 * Math.abs(0.5 - models[i].cdf(x));
      probs[i] = probs[i] == 0.0 ? r.nextDouble() : probs[i];
      sum += probs[i];
    }
    for (int i = 0; i < k; i++) {
      models[i].update(x, probs[i] / sum);
      coefs[i] = models[i].age / age;
    }
  }
  
  @Override
  public Model merge(Model model) {
    GMM m = (GMM)model;
    age = 0.0;
    for (int i = 0; i < k; i++) {
      models[i].merge(m.models[i]);
      age += models[i].age;
      coefs[i] = models[i].age;
    }
    for (int i = 0; i < k; i++) {
      coefs[i] = age == 0.0 ? 1.0 / k: coefs[i] / age;
    }
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    GMM m = (GMM)model;
    for (int i = 0; i < k; i++) {
      models[i].add(m.models[i], times);
      age += models[i].age;
      coefs[i] = models[i].age;
    }
    for (int i = 0; i < k; i++) {
      coefs[i] = age == 0.0 ? 1.0 / k: coefs[i] / age;
    }
    return null;
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  public double generate(Random r) {
    double vals = r.nextDouble();
    int idx = 0;
    double sum = coefs[idx];
    while (sum < vals) {
      idx ++;
      sum += coefs[idx];
    }
    return models[idx].generate(r);
  }
  
  public double pdf(double x) {
    double value = 0.0;
    for (int i = 0; i < k; i++) {
      value += coefs[i] * models[i].pdf(x);
    }
    return value;
  }
  
  public double prob(double x) {
    double value = 0.0;
    for (int i = 0; i < k; i++) {
      probs[i] = 1.0 - 2.0 * Math.abs(0.5 - models[i].cdf(x));
      value += coefs[i] * probs[i];
    }
    return value;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < k; i++) {
      sb.append(String.format("coef: %5f ", coefs[i]));
      sb.append(models[i]);
      if (i < k - 1) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }
}
