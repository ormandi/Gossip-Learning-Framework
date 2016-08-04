package gossipLearning.interfaces.functions;

public class TanhGrad extends Tanh {
  
  /**
   * @return 1 - tanh^2(x)
   */
  @Override
  public double execute(double x) {
    double tanh = super.execute(x);
    return (1.0 - (tanh * tanh)) / 2.0;
  }

}
