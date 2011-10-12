package gossipLearning.controls.observers.errorComputation;

/**
 * This class computes the mean absolute error of the specified expected 
 * and predicted values and can make the post process step.
 * @author István Hegedűs
 *
 */
public class MAError implements ErrorFunction {

  @Override
  public double computeError(double expected, double predicted) {
    return Math.abs(expected - predicted);
  }

  @Override
  public double postProcess(double meanError) {
    return meanError;
  }

}
