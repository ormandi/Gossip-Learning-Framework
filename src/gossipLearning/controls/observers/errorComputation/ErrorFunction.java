package gossipLearning.controls.observers.errorComputation;

/**
 * This interface describes the skeleton of an error function.
 * @author István Hegedűs
 *
 */
public interface ErrorFunction {
  /**
   * Computes the error based on the specified expected and predicted values
   * @param expected expected value
   * @param predicted predicted value
   * @return error value
   */
  public double computeError(double expected, double predicted);
  /**
   * Makes post processing step on the specified mean error (e.g. squared root)
   * @param meanError mean error
   * @return post processed mean error
   */
  public double postProcess(double meanError);
}
