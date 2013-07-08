package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.utils.SparseVector;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import peersim.config.Configuration;

public class MergeableLowRank extends LowRankDecomposition implements Mergeable<MergeableLowRank>, Partializable<MergeableLowRank> {
  private static final long serialVersionUID = -8892302266739538821L;
  private static final String PAR_DIMENSION = "MergeableLowRank.dimension";
  private static final String PAR_LAMBDA = "MergeableLowRank.lambda";
  private static final String PAR_ALPHA = "MergeableLowRank.alpha";
  
  public MergeableLowRank() {
    super();
  }
  
  public MergeableLowRank(MergeableLowRank a) {
    super(a);
  }
  
  public MergeableLowRank(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  @Override
  public Object clone() {
    return new MergeableLowRank(this);
  }
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
  }
  
  @Override
  public MergeableLowRank merge(MergeableLowRank model) {
    maxIndex = Math.max(maxIndex, model.maxIndex);
    for (Entry<Integer, SparseVector> e : model.columnModels.entrySet()) {
      // merge by averaging
      SparseVector v = columnModels.get(e.getKey());
      if (v == null) {
        columnModels.put(e.getKey(), e.getValue());
      } else {
        v.add(e.getValue()).mul(0.5);
      }
    }
    return this;
  }
  
  @Override
  public MergeableLowRank getModelPart(Set<Integer> indices) {
    return new MergeableLowRank(this);
    //return this;
  }

}
