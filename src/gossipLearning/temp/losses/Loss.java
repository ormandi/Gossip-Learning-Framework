package gossipLearning.temp.losses;

public interface Loss {
  public double loss(double fx, double y);
  public double lossGrad(double fx, double y);
}
