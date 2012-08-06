package gossipLearning.models.kernels;

import gossipLearning.utils.SparseVector;

public class RBFKernel implements Kernel {
  private double sigma = 0.1;
  
  @Override
  public double kernel(SparseVector x, SparseVector y) {
    final double dist = x.euclideanDistance(y);
    return Math.exp(- dist * dist / 2.0 / sigma / sigma);
  }

  public double getSigma() {
    return sigma;
  }

  public void setSigma(double sigma) {
    if (sigma <= 0.0) {
      throw new RuntimeException("Sigma (variance) couldn't be negative or 0!");
    }
    this.sigma = sigma;
  }
  
  

}
