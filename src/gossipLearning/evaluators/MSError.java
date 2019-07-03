package gossipLearning.evaluators;

/**
 * This class can compute the mean squared error.
 * 
 * @author István Hegedűs
 */
public class MSError extends ValueBasedEvaluator {
  private static final long serialVersionUID = 5356691632259042142L;
  
  public MSError() {
    super();
  }
  
  public MSError(MSError a) {
    super(a);
  }

  @Override
  public MSError clone() {
    return new MSError(this);
  }

  /**
   * @return the squared difference of the specified values
   */
  @Override
  public double getValue(double expected, double predicted) {
    return (expected - predicted) * (expected - predicted);
  }

  /**
   * @return the specified value
   */
  @Override
  public double postProcess(double meanValue) {
    return meanValue;
  }

}
