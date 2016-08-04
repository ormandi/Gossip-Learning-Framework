package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class Rectifier implements Function {

  /**
   * @return max(0,x)
   */
  @Override
  public double execute(double x) {
    return x > 0.0 ? x : 0.0;
  }

}
