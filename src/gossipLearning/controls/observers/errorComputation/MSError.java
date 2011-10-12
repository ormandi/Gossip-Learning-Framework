package gossipLearning.controls.observers.errorComputation;

/**
 * This class computes the mean squared error of the specified expected 
 * and predicted values and can make the post process step.
 * @author István Hegedűs
 *
 */
public class MSError implements ErrorFunction {

  @Override
  public double computeError(double expected, double predicted) {
    return (expected - predicted) * (expected - predicted);
  }

  @Override
  public double postProcess(double meanError) {
    return meanError;
  }

}
