package gossipLearning.models.recsys;

import java.util.HashMap;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class RecSysModel extends LowRankDecomposition {
  private static final long serialVersionUID = 237945358449513368L;
  private static final String PAR_DIMENSION = "RecSysModel.dimension";
  private static final String PAR_LAMBDA = "RecSysModel.lambda";
  private static final String PAR_ALPHA = "RecSysModel.alpha";
  
  public RecSysModel() {
    super();
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
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
  }

  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    double[] newVector;
    if (rowModel == null) {
      // initialize user-model if its null by uniform random numbers  on [0,1]
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = CommonState.r.nextDouble();
      }
      rowModel = new SparseVector(newVector);
    }
    SparseVector newUserModel = (SparseVector)rowModel.clone();
    age ++;
    
    newUserModel.mul(1.0 - alpha);
    for (VectorEntry e : instance) {
      SparseVector itemModel = columnModels.get(e.index);
      if (itemModel == null) {
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          newVector[i] = CommonState.r.nextDouble();
        }
        itemModel = new SparseVector(newVector);
        columnModels.put(e.index, itemModel);
      }
      
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double error = e.value - prediction;
      
      // update models
      newUserModel.add(itemModel, lambda * error);
      itemModel.mul(1.0 - alpha);
      itemModel.add(rowModel, lambda * error);
    }
    
    // return new user-model
    return newUserModel;
  }
  
}
