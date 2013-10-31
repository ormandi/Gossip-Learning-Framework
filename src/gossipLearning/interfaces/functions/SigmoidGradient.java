package gossipLearning.interfaces.functions;

public class SigmoidGradient extends Sigmoid {

  /**
   * @return sigmoid(x) * (1 - sigmoid(x))
   */
  @Override
  public double execute(double x) {
    return super.execute(x) * (1.0 - super.execute(x));
  }

}
