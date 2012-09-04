package gossipLearning.interfaces.kernels;

import gossipLearning.utils.SparseVector;

public class LinearKernel implements Kernel {
  public double kernel(SparseVector x, SparseVector y) {
    return x.mul(y);
  }
}
