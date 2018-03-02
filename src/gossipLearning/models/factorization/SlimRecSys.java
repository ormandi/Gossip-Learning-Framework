package gossipLearning.models.factorization;


public class SlimRecSys extends MergeableRecSys {
  private static final long serialVersionUID = -2675295901691742814L;
  private static final String PAR_DIMENSION = "SlimRecSys.dimension";
  private static final String PAR_LAMBDA = "SlimRecSys.lambda";
  private static final String PAR_ALPHA = "SlimRecSys.alpha";
  private static final String PAR_MIN = "SlimRecSys.min";
  private static final String PAR_MAX = "SlimRecSys.max";
  private static final String PAR_NUMITEMS = "SlimRecSys.numItems";
  
  public SlimRecSys(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public SlimRecSys(SlimRecSys a) {
    super(a);
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
  public SlimRecSys getModelPart() {
    return this;
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
