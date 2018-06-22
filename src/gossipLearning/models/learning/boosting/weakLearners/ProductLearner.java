package gossipLearning.models.learning.boosting.weakLearners;

import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Arrays;

import peersim.config.Configuration;

public class ProductLearner extends WeakLearner {
  private static final long serialVersionUID = 6400546153303842520L;
  
  private static final String PAR_NUMLEARNERS = "ProductLearner.numLearners";
  private static final String PAR_LEARNERNAME = "ProductLearner.learnerName";
  
  private final int numberOfLearners;
  
  private final String baseLearnerName;
  private WeakLearner[] baseLearners;
  
  public ProductLearner(String prefix, double lambda, long seed) {
    super(prefix, lambda, seed);
    numberOfLearners = Configuration.getInt(prefix + "." + PAR_NUMLEARNERS);
    baseLearnerName = Configuration.getString(prefix + "." + PAR_LEARNERNAME);
    baseLearners = new WeakLearner[numberOfLearners];
    for (int i = 0; i < numberOfLearners; i++) {
      try {
        baseLearners[i] = (WeakLearner)Class.forName(baseLearnerName).getConstructor(String.class, double.class, long.class).newInstance(prefix + "." + getClass().getSimpleName(), lambda, seed);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  public ProductLearner(ProductLearner a) {
    super(a);
    numberOfLearners = a.numberOfLearners;
    baseLearnerName = a.baseLearnerName;
    if (a.baseLearners != null) {
      baseLearners = new WeakLearner[numberOfLearners];
      for (int i = 0; i < numberOfLearners; i++) {
        baseLearners[i] = (WeakLearner)a.baseLearners[i].clone();
      }
    }
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    for (int i = 0; i < numberOfLearners; i++) {
      baseLearners[i].setParameters(numberOfClasses, numberOfFeatures);
    }
  }
  
  @Override
  public Object clone() {
    return new ProductLearner(this);
  }

  @Override
  public void update(SparseVector instance, double label, double[] weight) {
    double[] dist;
    double[] distribution = new double[numberOfClasses];
    Arrays.fill(distribution, 1.0);
    
    // get distributions
    for (int i = 0; i < numberOfLearners; i++) {
      dist = baseLearners[i].distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses; j++) {
        distribution[j] *= dist[j];
      }
    }
    
    // update learners
    double[] labels = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      if (i == label) {
        labels[i] = 1.0;
      } else {
        labels[i] = -1.0;
      }
    }
    for (int i = 0; i < numberOfLearners; i++) {
      dist = baseLearners[i].distributionForInstance(instance);
      for (int l = 0; l < numberOfClasses; l++) {
        double val = labels[l] * distribution[l] * dist[l];
        if (val < 0.0) {
          weight[l] *= -1.0;
          labels[l] *= -1.0;
        }
      }
      baseLearners[i].update(instance, label, weight);
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double[] distribution = new double[numberOfClasses];
    Arrays.fill(distribution, 1.0);
    double[] dist;
    for (int i = 0; i < numberOfLearners; i++) {
      dist = baseLearners[i].distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses; j++) {
        distribution[j] *= dist[j];
      }
    }
    return Utils.normalize(distribution);
  }

}
