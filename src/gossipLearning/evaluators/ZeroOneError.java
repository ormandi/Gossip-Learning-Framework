package gossipLearning.evaluators;

/**
 * This class can compute the zero-one error.
 * 
 * @author István Hegedűs
 */
public class ZeroOneError extends ValueBasedEvaluator {
  private static final long serialVersionUID = 3078153893635373513L;

  public ZeroOneError() {
    super();
  }
  
  public ZeroOneError(ZeroOneError a) {
    super(a);
  }
  
  @Override
  public ZeroOneError clone() {
    return new ZeroOneError(this);
  }

  /**
   * @return 0 if the expected value is equals to the predicted value 
   * and 1 otherwise
   */
  @Override
  public double getValue(double expected, double predicted) {
    return expected == predicted ? 0.0 : 1.0;
  }

  /**
   * @return the specified value
   */
  @Override
  public double postProcess(double meanValue) {
    return meanValue;
  }

}
