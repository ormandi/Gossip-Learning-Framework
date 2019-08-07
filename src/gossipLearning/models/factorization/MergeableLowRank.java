package gossipLearning.models.factorization;

import java.util.Random;

import peersim.core.CommonState;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;

public class MergeableLowRank extends LowRankDecomposition implements Mergeable, Partializable {
  private static final long serialVersionUID = -8892302266739538821L;
  public MergeableLowRank(String prefix) {
    super(prefix);
  }
  
  public MergeableLowRank(MergeableLowRank a) {
    super(a);
  }
  
  @Override
  public MergeableLowRank clone() {
    return new MergeableLowRank(this);
  }
  
  @Override
  public Model merge(Model model) {
    MergeableLowRank m = (MergeableLowRank)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (int i = 0; i < dimension; i++) {
      if (m.columnModels[i] == null) {
        continue;
      }
      if (columnModels[i] == null) {
        columnModels[i] = m.columnModels[i].clone();
      } else {
        for (int j = 0; j < columnModels[i].length; j++) {
          columnModels[i][j] *= 1.0 - modelWeight;
          columnModels[i][j] += modelWeight * m.columnModels[i][j];
        }
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
        columnModels[i] = m.columnModels[i].clone();
      } else {
        v.add(m.columnModels[i], times);
      }
    }
    return this;
  }*/
  
  @Override
  public Model getModelPart() {
    return getModelPart(CommonState.r);
  }
  
  @Override
  public Model getModelPart(Random r) {
    return new MergeableLowRank(this);
  }

}
