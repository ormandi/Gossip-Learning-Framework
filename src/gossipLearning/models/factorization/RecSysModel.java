package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class RecSysModel extends LowRankDecomposition {
  private static final long serialVersionUID = 237945358449513368L;
  private static final String PAR_MIN = "min";
  private static final String PAR_MAX = "max";
  private static final String PAR_LAMBDA = "lambda";
  
  protected final double minRating;
  protected final double maxRating;
  protected final double lambda;
  
  public RecSysModel(String prefix) {
    super(prefix);
    minRating = Configuration.getDouble(prefix + "." + PAR_MIN);
    maxRating = Configuration.getDouble(prefix + "." + PAR_MAX);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  public RecSysModel(RecSysModel a) {
    super(a);
    minRating = a.minRating;
    maxRating = a.maxRating;
    lambda = a.lambda;
  }
  
  @Override
  public Object clone() {
    return new RecSysModel(this);
  }
  
  @Override
  public double[] update(double[] rowModel, SparseVector instance) {
    if (rowModel == null) {
      // initialize user-model if its null by uniform random numbers  on [0,1]
      rowModel = initVector(true);
    }
    age ++;
    
    for (VectorEntry e : instance) {
      double[] itemModel = columnModels[e.index];
      if (itemModel == null) {
        itemModel = initVector(false);
        columnModels[e.index] = itemModel;
      }
      
      // get the prediction and the error
      double prediction = Utils.mul(rowModel, itemModel);
      double error = e.value - prediction;
      
      // update models
      for (int i = 0; i < k; i++) {
        rowModel[i] = (1.0 - lambda) * rowModel[i] + (eta * itemModel[i] * error);
        itemModel[i] = (1.0 - lambda) * itemModel[i] + (eta * rowModel[i] * error) ;
      }
      rowModel[k] = (1.0 - lambda) * rowModel[k] + (eta * error);
      itemModel[k + 1] = (1.0 - lambda) * itemModel[k + 1] + (eta * error);
    }
    // return new user-model
    return rowModel;
  }
  
  @Override
  public double predict(double[] rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    if (rowModel == null || columnModels[columnIndex] == null) {
      return 0;
    } else if (columnModels[columnIndex] == null) {
      return rowModel[k];
    }
    return Utils.mul(rowModel, columnModels[columnIndex]);
  }
  
  @Override
  protected double[] initVector(boolean isRow) {
    double initVal = Math.sqrt(maxRating / (k + 2));
    double[] newVector = new double[k + 2];
    for (int d = 0; d < k; d++) {
      newVector[d] = 0.5 + CommonState.r.nextDouble() * initVal;
    }
    // for bias term
    if (isRow) {
      newVector[k] = 0.5 + CommonState.r.nextDouble() * initVal;
      newVector[k + 1] = 1.0;
    } else {
      newVector[k] = 1.0;
      newVector[k + 1] = 0.5 + CommonState.r.nextDouble() * initVal;
    }
    return newVector;
  }
  
}
