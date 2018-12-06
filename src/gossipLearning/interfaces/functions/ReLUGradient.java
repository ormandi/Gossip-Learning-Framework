package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class ReLUGradient implements Function {

  @Override
  public double execute(double x) {
    return 0.0 < x ? 1.0 : 0.0;
  }

}
