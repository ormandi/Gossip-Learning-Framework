package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;
import peersim.core.CommonState;

public class RecSysNormedModel extends RecSysModel {
  private static final long serialVersionUID = 2162557238372224393L;
  private static final String PAR_DIMENSION = "RecSysNormedModel.dimension";
  private static final String PAR_LAMBDA = "RecSysNormedModel.lambda";
  private static final String PAR_ALPHA = "RecSysNormedModel.alpha";
  private static final String PAR_MIN = "RecSysNormedModel.min";
  private static final String PAR_MAX = "RecSysNormedModel.max";
  private static final String PAR_NUMITEMS = "RecSysNormedModel.numItems";
  
  public RecSysNormedModel(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public RecSysNormedModel(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA, String PAR_NUMITEMS, String PAR_MIN, String PAR_MAX) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_NUMITEMS, PAR_MIN, PAR_MAX);
  }
  
  public RecSysNormedModel(RecSysNormedModel a) {
    super(a);
  }
  
  public Object clone() {
    return new RecSysNormedModel(this);
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
      rowModel.normalize();
    }
    /*
    if (columnModels.size() == 0) {
      for (int i = 0; i < numItems; i++) {
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension + 1];
        for (int d = 0; d < dimension; d++) {
          //newVector[i] = CommonState.r.nextDouble() / dimension;
          newVector[d] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;
        }
        newVector[dimension] = 1.0;
        SparseVector itemModel = new SparseVector(newVector);
        itemModel.normalize();
        columnModels.put(i, itemModel);
      }
    }
    */
    age ++;
    
    //SparseVector copy = new SparseVector(rowModel);
    for (VectorEntry e : instance) {
      SparseVector itemModel = columnModels[e.index];
      if (itemModel == null) {
        //throw new RuntimeException("null itemModel!!!");
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension + 1];
        for (int i = 0; i < dimension; i++) {
          //newVector[i] = CommonState.r.nextDouble() / dimension;
          newVector[i] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;
        }
        newVector[dimension] = 1.0;
        itemModel = new SparseVector(newVector);
        itemModel.normalize();
        //columnModels.put(e.index, itemModel);
        columnModels[e.index] = itemModel;
      }
      
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double expected = (2.0 * e.value - minRating - maxRating)/(maxRating - minRating);
      double error = expected - prediction;
      //System.out.println(prediction + "\t" + expected + "\t" + error);
      
      // update models
      double bias = rowModel.get(dimension);
      rowModel.mul(1.0 - alpha);
      rowModel.add(dimension, bias - rowModel.get(dimension));
      rowModel.add(itemModel, lambda * error);
      
      if (updY) {
        bias = itemModel.get(dimension);
        itemModel.mul(1.0 - alpha);
        itemModel.add(rowModel, lambda * error);
        itemModel.add(dimension, bias - itemModel.get(dimension));
        
        itemModel.normalize();
      }
    }
    // return new user-model
    rowModel.normalize();
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
    double predicted = 0.0;
    if (rowModel == null) {
      predicted = 0.0;
    } else if (itemModel == null) {
      predicted = rowModel.get(dimension);
    } else {
      predicted = itemModel.mul(rowModel);
    }
    double ret = 0.5 * (predicted * (maxRating - minRating) + maxRating + minRating);
    //System.out.println(predicted + "\t" + ret);
    return ret;
  }
  
}
