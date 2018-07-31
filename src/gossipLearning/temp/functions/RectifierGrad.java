package gossipLearning.temp.functions;

import gossipLearning.interfaces.Function;

public class RectifierGrad implements Function {
  @Override
  public double execute(double x) {
    return 0.0 < x ? 0.0 : 1.0;
  }

}
