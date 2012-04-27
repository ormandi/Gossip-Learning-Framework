package gossipLearning.interfaces;

public interface ErrorEstimatorModel extends Model {
  /**
   * Returns the error rate of the model, was estimated on training set.
   * @return Estimated error
   */
  public double getError();
}
