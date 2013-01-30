package gossipLearning.models.recsys;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.core.CommonState;

public class Merge2RecSysModel extends MergeableRecSysModel {
  private static final long serialVersionUID = 1813513859792192800L;
  
  public Merge2RecSysModel() {
    super();
  }
  
  public Merge2RecSysModel(Merge2RecSysModel a) {
    super(a);
  }
  
  public Object clone() {
    return new Merge2RecSysModel(this);
  }

  public Merge2RecSysModel getModelPart(SparseVector rates, int numRandToGen) {
    Merge2RecSysModel result = new Merge2RecSysModel();
    /*for (VectorEntry e : rates) {
      result.itemModels.put(e.index, itemModels.get(e.index));
    }*/
    int num = Math.min(10, itemModels.size());
    Object[] indices = itemModels.keySet().toArray();
    Utils.arraxShuffle(CommonState.r, indices);
    for (int i = 1; i < num; i++) {
      result.itemModels.put((Integer)indices[i], itemModels.get(indices[i]));
    }
    return result;
  }

}
