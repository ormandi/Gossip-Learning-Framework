package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class Constant implements Function {
  
  /**
   * @return 1
   */
  @Override
  public double execute(double x) {
    return 1.0;
  }
  
}
