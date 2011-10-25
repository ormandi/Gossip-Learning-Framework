package gossipLearning.interfaces;

import java.util.Map;

public interface ProbabilityModel {
  public double[] distributionForInstance(Map<Integer, Double> instance);
}
