package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class RecSysModel extends LowRankDecomposition {
  private static final long serialVersionUID = 237945358449513368L;
  private static final String PAR_DIMENSION = "dimension";
  private static final String PAR_LAMBDA = "lambda";
  private static final String PAR_ALPHA = "alpha";
  private static final String PAR_MIN = "min";
  private static final String PAR_MAX = "max";
  private static final String PAR_NUMITEMS = "numItems";
  protected final double minRating;
  protected final double maxRating;
  
  public RecSysModel(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public RecSysModel(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA, String PAR_NUMITEMS, String PAR_MIN, String PAR_MAX) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS);
    minRating = Configuration.getDouble(prefix + "." + PAR_MIN);
    maxRating = Configuration.getDouble(prefix + "." + PAR_MAX);
  }
  
  public RecSysModel(RecSysModel a) {
    super(a);
    minRating = a.minRating;
    maxRating = a.maxRating;
  }
  
  public Object clone() {
    return new RecSysModel(this);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    return this.update(rowIndex, rowModel, instance, true);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance, boolean updY) {
    double[] newVector;
    if (rowModel == null) {
      // initialize user-model if its null by uniform random numbers  on [0,1]
      newVector = new double[dimension + 1];
      for (int i = 0; i < dimension; i++) {
        //newVector[i] = CommonState.r.nextDouble() / dimension;
        newVector[i] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;
      }
      newVector[dimension] = instance.sum() / instance.size();
      rowModel = new SparseVector(newVector);
    }
    age ++;
    
    //SparseVector copy = new SparseVector(rowModel);
    for (VectorEntry e : instance) {
      //SparseVector itemModel = columnModels.get(e.index);
      SparseVector itemModel = columnModels[e.index];
      if (itemModel == null) {
        itemModel = initVector();
        columnModels[e.index] = itemModel;
      }
      
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double error = e.value - prediction;
      
      // update models
      double bias = rowModel.get(dimension);
      rowModel.mul(1.0 - alpha);
      rowModel.add(dimension, bias - rowModel.get(dimension));
      rowModel.add(itemModel, lambda * error);
      
      if (updY) {
        itemModel.mul(1.0 - alpha);
        itemModel.add(rowModel, lambda * error);
        itemModel.add(dimension, 1.0 - itemModel.get(dimension));
      }
    }
    // return new user-model
    return rowModel;
  }
  
  /*@Override
  public RecSysModel getModelPart(Set<Integer> indices) {
    return new RecSysModel(this);
  }*/
  
  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    //SparseVector itemModel = columnModels.get(columnIndex);
    SparseVector itemModel = columnModels[columnIndex];
    if (rowModel == null) {
      return 0.0;
    }
    if (itemModel == null) {
      return rowModel.get(dimension);
    }
    return itemModel.mul(rowModel);
  }
  
  protected SparseVector initVector() {
    double[] newVector = new double[dimension];
    for (int d = 0; d < dimension; d++) {
      newVector[d] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;;
      //newVector[d] = CommonState.r.nextDouble();
    }
    return new SparseVector(newVector);
  }
  
}
