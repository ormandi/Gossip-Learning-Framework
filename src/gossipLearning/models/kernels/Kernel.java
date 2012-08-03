package gossipLearning.models.kernels;

import gossipLearning.utils.SparseVector;

public interface Kernel {
  public double kernel(SparseVector x, SparseVector y);

}
