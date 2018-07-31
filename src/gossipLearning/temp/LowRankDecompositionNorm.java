package gossipLearning.temp;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.HashMap;
import java.util.Map.Entry;

import peersim.config.Configuration;

public class LowRankDecompositionNorm implements MatrixBasedModel, FeatureExtractor {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "LowRankDecomposition.dimension";
  private static final String PAR_LAMBDA = "LowRankDecomposition.lambda";
  private static final String PAR_ALPHA = "LowRankDecomposition.alpha";
  
  protected double age;
  protected HashMap<Integer, SparseVector> columnModels;
  protected SparseVector eigenValues;
  protected final int dimension;
  // learning rate
  protected final double lambda;
  // regularization parameter
  protected final double alpha;
  protected int maxIndex;
  protected Matrix R;
  protected Matrix V;
  
  public LowRankDecompositionNorm(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
    age = 0.0;
    columnModels = new HashMap<Integer, SparseVector>();
    eigenValues = new SparseVector();
    maxIndex = 1;
  }
  
  public LowRankDecompositionNorm(LowRankDecompositionNorm a) {
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
  
  public LowRankDecompositionNorm(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    this.age = age;
    this.columnModels = columnModels;
    this.dimension = dimension;
    this.lambda = lambda;
    this.alpha = alpha;
    this.maxIndex = maxIndex;
  }
  
  @Override
  public Object clone() {
    return new LowRankDecompositionNorm(this);
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
    //newUserModel.mul(1.0 - alpha);
    double value = 0.0;
    double nu = lambda / Math.log(age + 1);
    //double nu = lambda / Math.sqrt((age * 2100) + 1);
    //double nu = lambda;
    
    double[] newVector;
    if (rowModel == null) {
      newVector = new double[dimension];
      for (int d = 0; d < dimension; d++) {
        newVector[d] = 1.0 / maxIndex;
        //newVector[d] = CommonState.r.nextDouble();
        //newVector[d] = 1.0 / Math.sqrt(maxIndex);
        //System.out.println(maxIndex);
      }
      rowModel = new SparseVector(newVector);
    }
    
    SparseVector newRowModel = (SparseVector)rowModel.clone();
    
    for (int j = 0; j <= maxIndex; j++) {
      SparseVector columnModel = columnModels.get(j);
      // initialize a new item-model by 0.01
      if (columnModel == null) {
        newVector = new double[dimension];
        for (int d = 0; d < dimension; d++) {
          newVector[d] = 1.0 / maxIndex;
          //newVector[d] = CommonState.r.nextDouble();
          //newVector[d] = 1.0 / Math.sqrt(maxIndex);
          //System.out.println(maxIndex);
        }
        columnModel = new SparseVector(newVector);
        columnModels.put(j, columnModel);
      }
      value = instance.get(j);
      for (int i = 0; i < dimension; i++) {
        // get the prediction and the error
        double ri = rowModel.get(i);
        double ci = columnModel.get(i);
        double prediction = ri * ci;
        double error = value - prediction;
        
        // update models
        newRowModel.add(i, ci * nu * error);
        //itemModel.mul(1.0 - alpha);
        columnModel.add(i, ri * nu * error);
        
        // deflate the value of the matrix
        value -= prediction;
      }
    }
    
    //TODO: compute corresponding eigenvalue
    getV();
    SparseVector norms = new SparseVector(dimension);
    for (int i = 0; i < dimension; i++) {
      double norm = 0.0;
      for (Entry<Integer, SparseVector> e : columnModels.entrySet()) {
        value = e.getValue().get(i);
        V.set(e.getKey(), i, value);
        norm = Utils.hypot(norm, value);
      }
      for (int j = 0; j < maxIndex + 1; j++) {
        V.set(j, i, V.get(j, i) / norm);
      }
      norms.put(i, norm);
    }
    for (int j = 0; j < maxIndex + 1; j++) {
      columnModels.get(j).div(norms);
    }
    newRowModel.pointMul(norms);
    
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
  
  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    InstanceHolder result = new InstanceHolder(instances.getNumberOfClasses(), dimension);
    for (int i = 0; i < instances.size(); i++) {
      result.add(extract(instances.getInstance(i)), instances.getLabel(i));
    }
    return result;
  }
  
  public int getDimension() {
    return dimension;
  }
  
  public SparseVector extract(SparseVector instance) {
    getV();
    Matrix res = V.mulLeft(instance);
    SparseVector result = new SparseVector(res.getRow(0));
    return result;
  }
  
  public Matrix getV() {
    if (R == null) {
      R = new Matrix(dimension, dimension);
      for (int i = 0; i < dimension; i++) {
        R.set(i, i, 1.0);
      }
    }
    if (V == null || V.getRowDimension() < maxIndex + 1) {
      V = new Matrix(maxIndex + 1, dimension);
    }
    return V;
  }
  
  public Matrix getUSi(SparseVector ui) {
    getV();
    if (ui == null) {
      return new Matrix(1, dimension);
    }
    Matrix USi = R.mulLeft(ui);
    return USi;
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
