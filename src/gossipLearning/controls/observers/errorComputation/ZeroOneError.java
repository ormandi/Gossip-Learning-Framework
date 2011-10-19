package gossipLearning.controls.observers.errorComputation;

public class ZeroOneError implements ErrorFunction {

  @Override
  public double computeError(double expected, double predicted) {
    if (expected * expected != 1.0){
      throw new RuntimeException("The ZeroOneError function can only handle class labels 1.0 or -1.0! The specified was: " + expected);
    }
    predicted = predicted > 0.0 ? 1.0 : -1.0;
    return expected == predicted ? 0.0 : 1.0;
  }

  @Override
  public double postProcess(double meanError) {
    return meanError;
  }

}
