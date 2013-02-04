package gossipLearning.models.recsys;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Map.Entry;

public class MergeableRecSysModel extends RecSysModel {
  private static final long serialVersionUID = -3036509993299163450L;
  
  public MergeableRecSysModel() {
    super();
  }
  
  public MergeableRecSysModel(MergeableRecSysModel a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableRecSysModel(this);
  }

  @Override
  public MergeableRecSysModel merge(LowRankDecomposition model) {
    for (Entry<Integer, SparseVector> e : model.itemModels.entrySet()) {
      SparseVector vector = itemModels.get(e.getKey());
      if (vector == null) {
        itemModels.put(e.getKey(), e.getValue());
      } else {
        vector.mul(0.5).add(e.getValue(), 0.5);
      }
    }
    return this;
  }
  
  public MergeableRecSysModel getModelPart(SparseVector rates, int numRandToGen) {
    MergeableRecSysModel result = new MergeableRecSysModel();
    result.age = age;
    result.dimension = dimension;
    for (VectorEntry e : rates) {
      SparseVector v = itemModels.get(e.index);
      if (v != null) {
        result.itemModels.put(e.index, v);
      }
    }
    return result;
  }

}
