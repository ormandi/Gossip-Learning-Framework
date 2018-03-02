package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.utils.SparseVector;

public class MergeableRecSys extends RecSysModel implements Mergeable<MergeableRecSys> {
  private static final long serialVersionUID = 2481904642423040181L;
  private static final String PAR_DIMENSION = "MergeableRecSys.dimension";
  private static final String PAR_NUMITEMS = "MergeableRecSys.numItems";
  private static final String PAR_LAMBDA = "MergeableRecSys.lambda";
  private static final String PAR_ALPHA = "MergeableRecSys.alpha";
  private static final String PAR_MIN = "MergeableRecSys.min";
  private static final String PAR_MAX = "MergeableRecSys.max";
  
  
  public MergeableRecSys(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public MergeableRecSys(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA, String PAR_NUMITEMS, String PAR_MIN, String PAR_MAX) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public MergeableRecSys(MergeableRecSys a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableRecSys(this);
  }
  
  @Override
  public MergeableRecSys merge(MergeableRecSys model) {
    double sum = age + model.age;
    double w = age / (sum == 0 ? 1 : sum);
    double mw = model.age / (sum == 0 ? 1 : sum);
    age = (age + model.age) / 2.0;
    if (age + 60 < model.age) {
      w = 0.0;
      mw = 1.0;
      age = model.age;
    }
    if (model.age + 60 < age) {
      w = 1.0;
      mw = 0.0;
    }
    for (int i = 0; i < origDimension; i++) {
    //for (Entry<Integer, SparseVector> e : model.columnModels.entrySet()) {
      // merge by averaging
      //SparseVector v = columnModels.get(e.getKey());
      SparseVector v = columnModels[i];
      if (model.columnModels[i] != null) {
        if (v == null) {
          columnModels[i] = (SparseVector)model.columnModels[i].clone();
        } else {
          //v.mul(0.5).add(model.columnModels[i], 0.5);
          v.mul(w).add(model.columnModels[i], mw);
        }
      }
    }
    return this;
  }
  
  @Override
  public MergeableRecSys getModelPart() {
    //return new MergeableRecSys(this);
    return this;
  }
}
