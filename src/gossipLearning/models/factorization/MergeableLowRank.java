package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.SparseVector;

public class MergeableLowRank extends LowRankDecomposition implements Mergeable {
  private static final long serialVersionUID = -8892302266739538821L;
  public MergeableLowRank(String prefix) {
    super(prefix);
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
  public Model merge(Model model) {
    MergeableLowRank m = (MergeableLowRank)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double weight = age / sum;
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (int i = 0; i < origDimension; i++) {
      if (m.columnModels[i] == null) {
        continue;
      }
      SparseVector v = columnModels[i];
      if (v == null) {
        columnModels[i] = (SparseVector)m.columnModels[i].clone();
      } else {
        v.mul(weight).add(m.columnModels[i], modelWeight);
      }
    }
    return this;
  }
  
  /*@Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeableLowRank m = (MergeableLowRank)model;
    //age = Math.max(age, m.age);
    //age += m.age * times;
    for (int i = 0; i < origDimension; i++) {
      if (m.columnModels[i] == null) {
        continue;
      }
      SparseVector v = columnModels[i];
      if (v == null) {
        columnModels[i] = (SparseVector)m.columnModels[i].clone();
      } else {
        v.add(m.columnModels[i], times);
      }
    }
    return this;
  }*/
  
  @Override
  public MergeableLowRank getModelPart() {
    return new MergeableLowRank(this);
  }

}
