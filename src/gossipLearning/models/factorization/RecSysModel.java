package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.HashMap;

import peersim.core.CommonState;

public class RecSysModel extends LowRankDecomposition {
  private static final long serialVersionUID = 237945358449513368L;
  private static final String PAR_DIMENSION = "RecSysModel.dimension";
  private static final String PAR_LAMBDA = "RecSysModel.lambda";
  private static final String PAR_ALPHA = "RecSysModel.alpha";
  
  public RecSysModel(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA);
  }
  
  public RecSysModel(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA);
  }
  
  public RecSysModel(RecSysModel a) {
    super(a);
  }
  
  public RecSysModel(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  public Object clone() {
    return new RecSysModel(this);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    double[] newVector;
    if (rowModel == null) {
      double max = 0.0;
      for (int i = 0; i < instance.size(); i++) {
        if (instance.valueAt(i) > max) {
          max = instance.valueAt(i);
        }
      }
      // initialize user-model if its null by uniform random numbers  on [0,1]
      newVector = new double[dimension + 1];
      for (int i = 0; i < dimension; i++) {
        //newVector[i] = CommonState.r.nextDouble() / dimension;
        newVector[i] = CommonState.r.nextDouble() * Math.sqrt(2.0*max) / dimension;
      }
      newVector[dimension] = instance.sum() / instance.size();
      rowModel = new SparseVector(newVector);
    }
    age ++;
    
    //SparseVector copy = new SparseVector(rowModel);
    for (VectorEntry e : instance) {
      SparseVector itemModel = columnModels.get(e.index);
      if (itemModel == null) {
        double max = 0.0;
        for (int i = 0; i < instance.size(); i++) {
          if (instance.valueAt(i) > max) {
            max = instance.valueAt(i);
          }
        }
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension + 1];
        for (int i = 0; i < dimension; i++) {
          //newVector[i] = CommonState.r.nextDouble() / dimension;
          newVector[i] = CommonState.r.nextDouble() * Math.sqrt(2.0*max) / dimension;
        }
        newVector[dimension] = 1.0;
        itemModel = new SparseVector(newVector);
        columnModels.put(e.index, itemModel);
      }
      
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double error = e.value - prediction;
      
      // update models
      double bias = rowModel.get(dimension);
      rowModel.mul(1.0 - alpha);
      rowModel.add(dimension, bias - rowModel.get(dimension));
      rowModel.add(itemModel, lambda * error);
      
      itemModel.mul(1.0 - alpha);
      itemModel.add(rowModel, lambda * error);
      itemModel.add(dimension, 1.0 - itemModel.get(dimension));
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
    SparseVector itemModel = columnModels.get(columnIndex);
    if (rowModel == null) {
      return 0.0;
    }
    if (itemModel == null) {
      return rowModel.get(dimension);
    }
    return itemModel.mul(rowModel);
  }
  
}
