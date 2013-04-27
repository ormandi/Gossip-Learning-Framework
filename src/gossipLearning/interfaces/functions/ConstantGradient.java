package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class ConstantGradient implements Function {
  
  @Override
  public double execute(double x) {
    return 1.0;
  }
  
}
