package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class RecSysModel2 extends LowRankDecomposition {
  private static final long serialVersionUID = 237945358449513368L;
  private static final String PAR_DIMENSION = "RecSysModel.dimension";
  private static final String PAR_LAMBDA = "RecSysModel.lambda";
  private static final String PAR_ALPHA = "RecSysModel.alpha";
  private static final String PAR_ORIGDIM = "RecSysModel.numItems";
  
  public RecSysModel2(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_ORIGDIM);
  }
  
  public RecSysModel2(RecSysModel2 a) {
    super(a);
  }
  
  public RecSysModel2(double age, SparseVector[] columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
  }
  
  public Object clone() {
    return new RecSysModel2(this);
  }
  
  public SparseVector update(int rowIndex, SparseVector userModel, SparseVector instance) {
    double[] newVector;
    if (userModel == null) {
      // initialize user-model if its null by uniform random numbers  on [0,1]
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = 0.1;
      }
      userModel = new SparseVector(newVector);
    }
    SparseVector newUserModel = (SparseVector)userModel.clone();
    age ++;
    
    newUserModel.mul(1.0 - alpha);
    double nu = lambda / Math.log(age + 1);
    double value;
    for (VectorEntry e : instance) {
      SparseVector itemModel = columnModels[e.index];
      if (itemModel == null) {
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          newVector[i] = 0.1;
        }
        itemModel = new SparseVector(newVector);
        columnModels[e.index] = itemModel;
      }
      //itemModel.mul(1.0 - alpha);
      
      value = e.value;
      for (int i = 0; i < dimension; i++) {
        // get the prediction and the error
        double ui = userModel.get(i);
        double ii = itemModel.get(i);
        double prediction = ui * ii;
        double error = value - prediction;
        
        // update models
        newUserModel.add(i, ii * nu * error);
        //itemModel.mul(1.0 - alpha);
        itemModel.add(i, ui * nu * error);
        
        // deflate the value of the matrix
        value -= prediction;
      }
      
      /*
      // get the prediction and the error
      double prediction = itemModel.mul(userModel);
      double error = e.value - prediction;
      
      // update models
      newUserModel.add(itemModel, lambda * error);
      itemModel.mul(1.0 - alpha);
      itemModel.add(userModel, lambda * error);
      */
    }
    
    // return new user-model
    return newUserModel;
  }
  
  /*@Override
  public RecSysModel getModelPart(Set<Integer> indices) {
    return new RecSysModel(this);
  }*/
  
}
