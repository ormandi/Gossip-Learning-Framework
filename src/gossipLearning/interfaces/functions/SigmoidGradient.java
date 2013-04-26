package gossipLearning.interfaces.functions;

public class SigmoidGradient extends Sigmoid {

  @Override
  public double execute(double x) {
    return super.execute(x) * (1.0 - super.execute(x));
  }

}
