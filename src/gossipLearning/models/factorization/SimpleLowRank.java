package gossipLearning.models.factorization;

import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

import java.util.HashMap;
import java.util.Map.Entry;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class SimpleLowRank extends LowRankDecomposition {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "SimpleLowRank.dimension";
  private static final String PAR_LAMBDA = "SimpleLowRank.lambda";
  private static final String PAR_ALPHA = "SimpleLowRank.alpha";
  
  protected double age;
  protected HashMap<Integer, SparseVector> columnModels;
  protected SparseVector eigenValues;
  protected int dimension;
  // learning rate
  protected double lambda;
  // regularization parameter
  protected double alpha;
  protected int maxIndex;
  protected Matrix V;
  
  public SimpleLowRank() {
    age = 0.0;
    columnModels = new HashMap<Integer, SparseVector>();
    eigenValues = new SparseVector();
    dimension = 10;
    lambda = 0.001;
    alpha = 0.0;
    maxIndex = 1;
  }
  
  public SimpleLowRank(SimpleLowRank a) {
    age = a.age;
    // for avoiding size duplications of the HashMap
    int size = 1;
    while (size <= a.columnModels.size()) {
      size <<= 1;
    }
    columnModels = new HashMap<Integer, SparseVector>(size, 0.9f);
    for (Entry<Integer, SparseVector> e : a.columnModels.entrySet()) {
      columnModels.put(e.getKey().intValue(), (SparseVector)e.getValue().clone());
    }
    if (a.eigenValues != null) {
      eigenValues = new SparseVector(a.eigenValues);
    } else {
      eigenValues = new SparseVector();
    }
    dimension = a.dimension;
    lambda = a.lambda;
    alpha = a.alpha;
    maxIndex = a.maxIndex;
  }
  
  public SimpleLowRank(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    this.age = age;
    this.columnModels = columnModels;
    this.dimension = dimension;
    this.lambda = lambda;
    this.alpha = alpha;
    this.maxIndex = maxIndex;
  }
  
  @Override
  public Object clone() {
    return new SimpleLowRank(this);
  }
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
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
  
  public void setDimension(int dimension) {
    this.dimension = dimension;
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
