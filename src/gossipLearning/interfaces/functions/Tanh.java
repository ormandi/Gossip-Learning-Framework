package gossipLearning.interfaces.functions;

import gossipLearning.interfaces.Function;

public class Tanh implements Function {

  /**
   * @return (e^x - e^-x) / (e^x + e^-x)
   */
  @Override
  public double execute(double x) {
    double exp = Math.exp(x);
    double invexp = 1.0 / exp;
    double res = (exp - invexp) / (exp + invexp);
    return 0.5 + (res / 2.0);
  }

}
