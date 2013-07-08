package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.utils.SparseVector;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import peersim.config.Configuration;

public class MergeableRecSys extends RecSysModel implements Mergeable<MergeableRecSys>, Partializable<MergeableRecSys> {
  private static final long serialVersionUID = 2481904642423040181L;
  private static final String PAR_DIMENSION = "MergeableRecSys.dimension";
  private static final String PAR_LAMBDA = "MergeableRecSys.lambda";
  private static final String PAR_ALPHA = "MergeableRecSys.alpha";
  
  public MergeableRecSys() {
    super();
  }
  
  public MergeableRecSys(MergeableRecSys a) {
    super(a);
  }
  
  public MergeableRecSys(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  public Object clone() {
    return new MergeableRecSys(this);
  }
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
  }

  @Override
  public MergeableRecSys merge(MergeableRecSys model) {
    for (Entry<Integer, SparseVector> e : model.columnModels.entrySet()) {
      // merge by averaging
      SparseVector v = columnModels.get(e.getKey());
      if (v == null) {
        columnModels.put(e.getKey(), e.getValue());
      } else {
        v.mul(0.5).add(e.getValue(), 0.5);
      }
    }
    return this;
  }
  
  @Override
  public MergeableRecSys getModelPart(Set<Integer> indices) {
    return new MergeableRecSys(this);
    //return this;
  }
}
