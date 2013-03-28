package gossipLearning.models.recsys;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Map.Entry;
import java.util.Set;

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

  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    double[] newVector;
    if (rowModel == null) {
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
  
  @Override
  public RecSysModel merge(LowRankDecomposition model) {
    for (Entry<Integer, SparseVector> e : model.itemModels.entrySet()) {
      // store the new information
      //itemModels.put(e.getKey(), e.getValue());
      // merge by averaging
      SparseVector v = itemModels.get(e.getKey());
      if (v == null) {
        itemModels.put(e.getKey(), e.getValue());
      } else {
        v.mul(0.5).add(e.getValue(), 0.5);
      }
    }
    // only store recv model
    //this.itemModels = model.itemModels;
    return this;
  }
  
  public RecSysModel getModelPart(Set<Integer> indices) {
    return this;
  }
  
}
