package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;

public class MergeableRecSys extends RecSysModel implements Mergeable, Addable, Partializable {
  private static final long serialVersionUID = 2481904642423040181L;
  
  public MergeableRecSys(String prefix) {
    super(prefix);
  }
  
  public MergeableRecSys(MergeableRecSys a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableRecSys(this);
  }
  
  @Override
  public Model merge(Model model) {
    MergeableRecSys m = (MergeableRecSys)model;
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
    
    /*double w = age / (sum == 0 ? 1 : sum);
    double mw = m.age / (sum == 0 ? 1 : sum);
    age = (age + m.age) / 2.0;
    if (age + 60 < m.age) {
      w = 0.0;
      mw = 1.0;
      age = m.age;
    }
    if (m.age + 60 < age) {
      w = 1.0;
      mw = 0.0;
    }
    for (int i = 0; i < origDimension; i++) {
    //for (Entry<Integer, SparseVector> e : model.columnModels.entrySet()) {
      // merge by averaging
      //SparseVector v = columnModels.get(e.getKey());
      SparseVector v = columnModels[i];
      if (m.columnModels[i] != null) {
        if (v == null) {
          columnModels[i] = (SparseVector)m.columnModels[i].clone();
        } else {
          //v.mul(0.5).add(model.columnModels[i], 0.5);
          v.mul(w).add(m.columnModels[i], mw);
        }
      }
    }*/
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeableRecSys m = (MergeableRecSys)model;
    age += times * m.age;
    for (int i = 0; i < dimension; i++) {
      if (m.columnModels[i] != null) {
        if (columnModels[i] == null) {
          columnModels[i] = new double[m.columnModels[i].length];
        }
        for (int j = 0; j < columnModels[i].length; j++) {
          columnModels[i][j] += times * m.columnModels[i][j];
        }
      }
    }
    return this;
  }
  
  @Override
  public MergeableRecSys getModelPart() {
    return new MergeableRecSys(this);
  }
}
