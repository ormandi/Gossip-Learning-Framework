package gossipLearning.temp;

import gossipLearning.models.factorization.MergeableRecSys;


public class SlimRecSys extends MergeableRecSys {
  private static final long serialVersionUID = -2675295901691742814L;
  public SlimRecSys(String prefix) {
    super(prefix);
  }
  
  public SlimRecSys(SlimRecSys a) {
    super(a);
  }
  
  public Object clone() {
    return new SlimRecSys(this);
  }
  
  @Override
  public SlimRecSys getModelPart() {
    return new SlimRecSys(this);
    /*
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
    */
  }

}
