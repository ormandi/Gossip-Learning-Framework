package gossipLearning.interfaces.models;

public interface ErrorEstimatorModel extends LearningModel {
  /**
   * Returns the error rate of the model, was estimated on training set.
   * @return Estimated error
   */
  public double getError();
}
