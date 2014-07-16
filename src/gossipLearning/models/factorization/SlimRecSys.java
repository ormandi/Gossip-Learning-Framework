package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;

import java.util.HashMap;
import java.util.Set;

public class SlimRecSys extends MergeableRecSys {
  private static final long serialVersionUID = -2675295901691742814L;
  private static final String PAR_DIMENSION = "SlimRecSys.dimension";
  private static final String PAR_LAMBDA = "SlimRecSys.lambda";
  private static final String PAR_ALPHA = "SlimRecSys.alpha";
  
  public SlimRecSys(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA);
  }
  
  public SlimRecSys(SlimRecSys a) {
    super(a);
  }
  
  public SlimRecSys(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  public Object clone() {
    return new SlimRecSys(this);
  }
  
  @Override
  public SlimRecSys merge(MergeableRecSys model) {
    super.merge(model);
    return this;
  }
  
  @Override
  public SlimRecSys getModelPart(Set<Integer> indices) {
    // for avoiding size duplications of the HashMap
    int size = 1;
    while (size <= indices.size()) {
      size <<= 1;
    }
    HashMap<Integer, SparseVector> columnModels = new HashMap<Integer, SparseVector>(size, 0.9f);
    for (int index : indices) {
      SparseVector v = this.columnModels.get(index);
      if (v != null) {
        columnModels.put(index, v);
      }
    }
    return new SlimRecSys(age, columnModels, dimension, lambda, alpha, maxIndex);
  }

}
