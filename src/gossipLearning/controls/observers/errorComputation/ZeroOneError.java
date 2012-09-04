package gossipLearning.controls.observers.errorComputation;

/**
 * This class computes the 0-1 error of the specified expected 
 * and predicted values and can make the post process step.
 * @author István Hegedűs
 *
 */
public class ZeroOneError implements ErrorFunction {

  @Override
  public double computeError(double expected, double predicted) {
    return expected == predicted ? 0.0 : 1.0;
  }

  @Override
  public double postProcess(double meanError) {
    return meanError;
  }

}
