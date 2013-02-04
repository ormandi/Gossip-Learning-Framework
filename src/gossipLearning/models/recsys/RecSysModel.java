package gossipLearning.models.recsys;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Map.Entry;

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
  
  public Object clone() {
    return new RecSysModel(this);
  }
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
  }

  public SparseVector update(SparseVector ratings, SparseVector userModel) {
    double[] newVector;
    if (userModel == null) {
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = CommonState.r.nextDouble();
      }
      userModel = new SparseVector(newVector);
    }
    SparseVector newUserModel = (SparseVector)userModel.clone();
    age ++;
    
    newUserModel.mul(1.0 - alpha);
    for (VectorEntry e : ratings) {
      // get the prediction and the error
      SparseVector itemModel = itemModels.get(e.index);
      // initialize a new item-model by uniform random numbers [0,1]
      if (itemModel == null) {
        newVector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          newVector[i] = CommonState.r.nextDouble();
        }
        itemModel = new SparseVector(newVector);
        itemModels.put(e.index, itemModel);
      }
      double prediction = itemModel.mul(userModel);
      double error = e.value - prediction;
      
      // update models
      newUserModel.add(itemModel, lambda * error);
      itemModel.mul(1.0 - alpha);
      itemModel.add(userModel, lambda * error);
    }
    
    // return new user-model
    return newUserModel;
  }
  
  @Override
  public RecSysModel merge(LowRankDecomposition model) {
    for (Entry<Integer, SparseVector> e : model.itemModels.entrySet()) {
      itemModels.put(e.getKey(), e.getValue());
    }
    return this;
  }

}
