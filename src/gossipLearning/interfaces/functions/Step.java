package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class Step implements Function {

  /**
   * @return 1, if x > 0 <br/> 0, otherwise
   */
  @Override
  public double execute(double x) {
    return 0 < x ? 1.0 : 0.0;
  }

}
