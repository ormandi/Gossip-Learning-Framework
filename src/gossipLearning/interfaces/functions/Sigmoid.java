package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class Sigmoid implements Function {

  /**
   * @return 1/(1 + e^-x)
   */
  @Override
  public double execute(double x) {
    return 1.0 / (1.0 + Math.exp(-x));
  }

}
