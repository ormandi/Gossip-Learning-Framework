package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class ReLU implements Function {

  @Override
  public double execute(double x) {
    return 0.0 < x ? x : 0.0;
  }

}
