package gossipLearning.models.learning.boosting.weakLearners;

import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Random;

import peersim.config.Configuration;

public class AdaLineLearner extends WeakLearner {
  private static final long serialVersionUID = -1540156152482197419L;
  
  private static final String PAR_LAMBDA = "AdaLineLearner.lambda";
  
  private SparseVector w;
  private double[] v;
  private int numberOfClasses;
  private double lambda;
  private Random r;
  private long seed;
  
  private static long c;

  public AdaLineLearner(String prefix) {
    super(prefix);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    seed = Configuration.getLong("random.seed");
    r = new Random(seed | c++);
    w = new SparseVector();
    age = 0.0;
  }
  
  public AdaLineLearner(AdaLineLearner a) {
    super(a.prefix);
    numberOfClasses = a.numberOfClasses;
    age = a.age;
    lambda = a.lambda;
    this.seed = a.seed;
    r = new Random(seed | c++);
    w = (SparseVector)a.w.clone();
    v = new double[numberOfClasses];
    if (a.v != null) {
      for (int i = 0; i < numberOfClasses; i++) {
        v[i] = a.v[i];
      }
    }
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
      v[i] = r.nextBoolean() ? 1.0 : -1.0;
    }
  }

  @Override
  public Object clone() {
    return new AdaLineLearner(this);
  }

  @Override
  public void update(SparseVector instance, double label, double[] weight) {
    age ++;
    double nu = 1.0 / (double) (age * lambda); // regularized
    double innerProd = w.mul(instance);
    double[] distribution = distributionForInstance(instance);
    double yl;
    double exp;
    
    // update w
    w.mul(1.0 - 1.0 / age);
    SparseVector grad = new SparseVector(instance.size());
    for (int l = 0; l < numberOfClasses; l++) {
      yl = (label == l) ? 1.0 : -1.0;
      exp = Math.exp(-yl * distribution[l]);
      grad.add(instance, -weight[l] * exp * yl * v[l]);
    }
    w.add(grad, -nu);
    
    // update v
    for (int l = 0; l < numberOfClasses; l++) {
      yl = (label == l) ? 1.0 : -1.0;
      exp = Math.exp(-yl * distribution[l]);
      v[l] += (1.0/age) * weight[l] * exp * yl * innerProd;
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double[] distribution = new double[numberOfClasses];
    double innerProd = w.mul(instance);
    for (int i = 0; i < numberOfClasses; i++) {
      distribution[i] = v[i] * innerProd;
    }
    return Utils.normalize(distribution);
  }

}
