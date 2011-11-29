package gossipLearning.models.weakLearners;

import java.util.Arrays;
import java.util.Map;

import peersim.config.Configuration;

import gossipLearning.interfaces.WeakLearner;
import gossipLearning.utils.Utils;

public class SequenceLearner extends WeakLearner {
  private static final long serialVersionUID = 6400546153303842520L;
  
  private static final String PAR_NUMLEARNERS = "numLearners";
  private static final String PAR_LEARNERNAME = "learnerName";
  
  private int numberOfClasses;
  private int numberOfLearners;
  
  private String baseLearnerName;
  private WeakLearner[] baseLearners;
  
  public SequenceLearner() {
  }
  
  public SequenceLearner(SequenceLearner a) {
    numberOfClasses = a.numberOfClasses;
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
  public void init(String prefix) {
    numberOfLearners = Configuration.getInt(prefix + "." + PAR_NUMLEARNERS);
    baseLearnerName = Configuration.getString(prefix + "." + PAR_LEARNERNAME);
    baseLearners = new WeakLearner[numberOfLearners];
    for (int i = 0; i < numberOfLearners; i++) {
      try {
        baseLearners[i] = (WeakLearner)Class.forName(baseLearnerName).newInstance();
        baseLearners[i].init(prefix);
      } catch (Exception e) {
        throw new RuntimeException(e);
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
    for (int i = 0; i < numberOfLearners; i++) {
      baseLearners[i].setNumberOfClasses(numberOfClasses);
    }
  }

  @Override
  public Object clone() {
    return new SequenceLearner(this);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label, double[] weight) {
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
    
    /*double energy;
    double prevEnergy = Double.MAX_VALUE;
    int i = 0;
    while(true) {
      baseLearners[i].update(instance, label, weight);
      dist = baseLearners[i].distributionForInstance(instance);
      energy = 0.0;
      for (int j = 0; j < numberOfClasses; j++) {
        energy += weight[j] * Math.exp((label == j ? 1.0 : -1.0) * dist[j]);
      }
      if (energy >= prevEnergy) {
        break;
      }
      prevEnergy = energy;
      for (int j = 0; j < numberOfClasses; j++) {
        if (dist[j] < 0.0) {
          weight[j] *= -1.0;
        }
      }
      i ++;
      i %= numberOfLearners;
    }*/
  }

  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
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
