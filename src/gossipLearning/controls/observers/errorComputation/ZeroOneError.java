package gossipLearning.controls.observers.errorComputation;

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
