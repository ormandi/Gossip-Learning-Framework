package gossipLearning.models.learning.boosting.weakLearners;

import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Arrays;

import peersim.config.Configuration;

public class SequentialLearner extends WeakLearner {
  private static final long serialVersionUID = 6400546153303842520L;
  
  private static final String PAR_NUMLEARNERS = "numLearners";
  private static final String PAR_LEARNERNAME = "learnerName";
  
  private final int numberOfLearners;
  
  private final String baseLearnerName;
  private WeakLearner[] baseLearners;
  
  public SequentialLearner(String prefix, double lambda, long seed) {
    super(prefix, lambda, seed);
    numberOfLearners = Configuration.getInt(prefix + "." + getClass().getSimpleName() + "." + PAR_NUMLEARNERS);
    baseLearnerName = Configuration.getString(prefix + "." + getClass().getSimpleName() + "." + PAR_LEARNERNAME);
    baseLearners = new WeakLearner[numberOfLearners];
    for (int i = 0; i < numberOfLearners; i++) {
      try {
        baseLearners[i] = (WeakLearner)Class.forName(baseLearnerName).getConstructor(String.class, double.class, long.class).newInstance(prefix + "." + getClass().getSimpleName(), lambda, seed);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  public SequentialLearner(SequentialLearner a) {
    super(a);
    numberOfLearners = a.numberOfLearners;
    baseLearnerName = a.baseLearnerName;
    if (a.baseLearners != null) {
      baseLearners = new WeakLearner[numberOfLearners];
      for (int i = 0; i < numberOfLearners; i++) {
        baseLearners[i] = a.baseLearners[i].clone();
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
  public SequentialLearner clone() {
    return new SequentialLearner(this);
  }

  @Override
  public void update(SparseVector instance, double label, double[] weight) {
    double[] dist;
    for (int i = 0; i < numberOfLearners; i++) {
      baseLearners[i].update(instance, label, weight);
      dist = baseLearners[i].distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses; j++) {
        if (dist[j] < 0.0) {
          weight[j] *= -1.0;
        }
      }
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
