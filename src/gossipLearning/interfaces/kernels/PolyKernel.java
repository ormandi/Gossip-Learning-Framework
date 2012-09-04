package gossipLearning.interfaces.kernels;

import gossipLearning.utils.SparseVector;

public class PolyKernel implements Kernel {

  @Override
  public double kernel(SparseVector x, SparseVector y) {
    return Math.pow(x.mul(y), 2.0); 
  }

}
