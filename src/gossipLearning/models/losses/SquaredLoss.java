package gossipLearning.models.losses;

public class SquaredLoss implements Loss {
  
  /**
   * Returns the loss part of the error function i.e. without the
   * regression part
   */
  @Override
  public double loss(double fx, double y) {
    final double diff = y - fx;
    return 0.5 * diff * diff;
  }

  /**
   * Returns the first derivative of loss part of the error function 
   * i.e. without the regression part
   */
  @Override
  public double lossGrad(double fx, double y) {
    return y - fx;
  }

}
