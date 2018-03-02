package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.utils.SparseVector;

public class MergeableLowRank extends LowRankDecomposition implements Mergeable<MergeableLowRank> {
  private static final long serialVersionUID = -8892302266739538821L;
  private static final String PAR_DIMENSION = "MergeableLowRank.dimension";
  private static final String PAR_ORIGDIM = "MergeableLowRank.origdim";
  private static final String PAR_LAMBDA = "MergeableLowRank.lambda";
  private static final String PAR_ALPHA = "MergeableLowRank.alpha";
  
  public MergeableLowRank(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_ORIGDIM);
  }
  
  public MergeableLowRank(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA, String PAR_ORIGDIM) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_ORIGDIM);
  }
  
  public MergeableLowRank(MergeableLowRank a) {
    super(a);
  }
  
  public MergeableLowRank(double age, SparseVector[] columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  @Override
  public Object clone() {
    return new MergeableLowRank(this);
  }
  
  @Override
  public MergeableLowRank merge(MergeableLowRank model) {
    double sum = age + model.age;
    if (sum == 0) {
      return this;
    }
    double weight = age / sum;
    double modelWeight = model.age / sum;
    age = Math.max(age, model.age);
    for (int i = 0; i < origDimension; i++) {
      if (model.columnModels[i] == null) {
        continue;
      }
      SparseVector v = columnModels[i];
      if (v == null) {
        columnModels[i] = (SparseVector)model.columnModels[i].clone();
      } else {
        v.mul(weight).add(model.columnModels[i], modelWeight);
      }
    }
    return this;
  }
  
  @Override
  public MergeableLowRank getModelPart() {
    return this;
  }

}
