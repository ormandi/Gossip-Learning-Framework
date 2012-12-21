package gossipLearning.evaluators;

/**
 * This class can compute the mean absolute error.
 * 
 * @author István Hegedűs
 */
public class MAError extends ValueBasedEvaluator {
  private static final long serialVersionUID = -481897520358225879L;
  
  public MAError() {
    super();
  }
  
  public MAError(MAError a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new MAError(this);
  }

  @Override
  public double getValue(double expected, double predicted) {
    return Math.abs(expected - predicted);
  }

  @Override
  public double postProcess(double meanValue) {
    return meanValue;
  }

}
