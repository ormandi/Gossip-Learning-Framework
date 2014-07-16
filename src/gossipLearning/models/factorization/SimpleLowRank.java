package gossipLearning.models.factorization;

import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

import java.util.HashMap;

import peersim.core.CommonState;

public class SimpleLowRank extends LowRankDecomposition {
  private static final long serialVersionUID = 4718668916550720442L;
  private static final String PAR_DIMENSION = "SimpleLowRank.dimension";
  private static final String PAR_LAMBDA = "SimpleLowRank.lambda";
  private static final String PAR_ALPHA = "SimpleLowRank.alpha";
  
  public SimpleLowRank(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA);
    age = 0.0;
    columnModels = new HashMap<Integer, SparseVector>();
    eigenValues = new SparseVector();
    maxIndex = 1;
  }
  
  public SimpleLowRank(SimpleLowRank a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new SimpleLowRank(this);
  }
  
  @Override
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    // rowIndex - userID
    // rowModel - userModel
    // instance - row of the matrix
    if (maxIndex < instance.maxIndex()) {
      maxIndex = instance.maxIndex();
    }
    
    age ++;
    double value = 0.0;
    double nu = lambda / Math.log(age + 1);
    
    double[] newVector;
    if (rowModel == null) {
      newVector = new double[dimension];
      for (int d = 0; d < dimension; d++) {
        newVector[d] = CommonState.r.nextDouble();
      }
      rowModel = new SparseVector(newVector);
    }
    
    SparseVector newRowModel = (SparseVector)rowModel.clone();
    
    for (int j = 0; j <= maxIndex; j++) {
      SparseVector columnModel = columnModels.get(j);
      // initialize a new column-model
      if (columnModel == null) {
        newVector = new double[dimension];
        for (int d = 0; d < dimension; d++) {
          newVector[d] = CommonState.r.nextDouble();
        }
        columnModel = new SparseVector(newVector);
        columnModels.put(j, columnModel);
      }
      value = instance.get(j);
      
      double prediction = rowModel.mul(columnModel);
      double error = value - prediction;
      newRowModel.add(columnModel, nu * error);
      columnModel.add(rowModel, nu * error);
    }
    
    // return new user-model
    return newRowModel;
  }
  
  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    SparseVector itemModel = columnModels.get(columnIndex);
    if (itemModel == null || rowModel == null) {
      return 0.0;
    }
    return itemModel.mul(rowModel);
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  public int getDimension() {
    return dimension;
  }
  
  public SparseVector extract(SparseVector instance) {
    // do nothing
    return instance;
  }
  
  public Matrix getV() {
    return null;
  }
  
  public Matrix getUSi(SparseVector ui) {
    return null;
  }
  
  public SparseVector getEigenValues() {
    return eigenValues;
  }
  
  @Override
  public String toString() {
    //return columnModels.toString();
    return getV().toString();
  }

}
