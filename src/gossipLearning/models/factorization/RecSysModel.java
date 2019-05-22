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
      double delta;
      for (int i = 0; i < k; i++) {
        delta = eta * (lambda * rowModel[i] - error * itemModel[i]);
        itemModel[i] -= eta * (lambda * itemModel[i] - error * rowModel[i]);
        rowModel[i] -= delta;
      }
      // TODO: should not be used for federated
      rowModel[k] += eta * error;
      itemModel[k + 1] += eta * error;
      //rowModel[k] -= eta * (lambda * rowModel[k] - error);
      //itemModel[k + 1] -= eta * (lambda * itemModel[k + 1] - error);
    }
    // return new user-model
    return rowModel;
  }
  
  @Override
  public double predict(double[] rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    if (rowModel == null && columnModels[columnIndex] == null) {
      return (maxRating + minRating) / 2.0;
    } else if (columnModels[columnIndex] == null) {
      return rowModel[k];
    } else if (rowModel == null) {
      return columnModels[columnIndex][k + 1];
    }
    return Math.min(Math.max(Utils.mul(rowModel, columnModels[columnIndex]), minRating), maxRating);
  }
  
  @Override
  protected double[] initVector(boolean isRow) {
    //double initVal = Math.sqrt(2.0 * (maxRating - minRating) / k);
    double initVal = Math.sqrt((maxRating - minRating) / k);
    double[] newVector = new double[k + 2];
    for (int d = 0; d < k; d++) {
      //newVector[d] = (CommonState.r.nextDouble() - 0.5) * initVal;
      newVector[d] = CommonState.r.nextDouble() * initVal;
    }
    // for bias term
    if (isRow) {
      //newVector[k] = (minRating + maxRating) / 4.0;
      newVector[k] = minRating / 2.0;
      newVector[k + 1] = 1.0;
    } else {
      newVector[k] = 1.0;
      //newVector[k + 1] = (minRating + maxRating) / 4.0;
      newVector[k + 1] = minRating / 2.0;
    }
    return newVector;
  }
  
}
