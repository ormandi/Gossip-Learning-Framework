package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;

public class LowRankDecomposition implements MatrixBasedModel, FeatureExtractor, Partializable {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "LowRankDecomposition.dimension";
  private static final String PAR_ORIGDIM = "LowRankDecomposition.origdim";
  private static final String PAR_LAMBDA = "LowRankDecomposition.lambda";
  private static final String PAR_ALPHA = "LowRankDecomposition.alpha";
  
  protected double age;
  protected final SparseVector[] columnModels;
  // size of the reduced dimensions
  protected final int dimension;
  // learning rate
  protected final double lambda;
  // regularization parameter
  protected final double alpha;
  // size of the original dimension
  protected final int origDimension;
  protected Matrix R;
  protected Matrix V;
  
  protected boolean isUpdated = true;
  
  public LowRankDecomposition(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_ORIGDIM);
  }
  
  public LowRankDecomposition(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA, String PAR_ORIGDIM) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
    age = 0.0;
    origDimension = Configuration.getInt(prefix + "." + PAR_ORIGDIM);
    columnModels = new SparseVector[origDimension];
  }
  
  public LowRankDecomposition(LowRankDecomposition a) {
    age = a.age;
    dimension = a.dimension;
    lambda = a.lambda;
    alpha = a.alpha;
    origDimension = a.origDimension;
    columnModels = new SparseVector[origDimension];
    for (int i = 0; i < origDimension; i++) {
      columnModels[i] = a.columnModels[i] == null ? null : (SparseVector)a.columnModels[i].clone();
    }
  }
  
  public LowRankDecomposition(double age, SparseVector[] columnModels, int dimension, double lambda, double alpha, int origDimension) {
    this.age = age;
    this.columnModels = columnModels;
    this.dimension = dimension;
    this.lambda = lambda;
    this.alpha = alpha;
    this.origDimension = origDimension;
  }
  
  @Override
  public Object clone() {
    return new LowRankDecomposition(this);
  }
  
  @Override
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    return this.update(rowIndex, rowModel, instance, true);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance, boolean updY) {
    // rowIndex - userID
    // rowModel - userModel
    // instance - row of the matrix
    age ++;
    double value = 0.0;
    double nu = lambda;
    
    // initialize a new row-model
    if (rowModel == null) {
      rowModel = initVector();
    }
    
    SparseVector newRowModel = (SparseVector)rowModel.clone();
    
    for (int j = 0; j < origDimension; j++) {
      SparseVector columnModel = columnModels[j];
      if (columnModel == null) {
        columnModel = initVector();
        columnModels[j] = columnModel;
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
        columnModel.add(i, ri * nu * error);
        
        // deflate the value of the matrix
        value -= prediction;
      }
    }
    isUpdated = true;
    
    // return new user-model
    return newRowModel;
  }
  
  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    //SparseVector itemModel = columnModels.get(columnIndex);
    SparseVector itemModel = columnModels[columnIndex];
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
  
  @Override
  public SparseVector extract(SparseVector instance) {
    if (isUpdated) {
      getV();
    }
    Matrix res = V.mulLeft(instance);
    SparseVector result = new SparseVector(res.getRow(0));
    return result;
  }
  
  @Override
  public Matrix getV() {
    if (!isUpdated) {
      return V;
    }
    if (R == null || V == null) {
      R = new Matrix(dimension, dimension);
      V = new Matrix(origDimension, dimension);
    }
    for (int i = 0; i < dimension; i++) {
      double norm = 0.0;
      for (int j = 0; j < origDimension; j++) {
        if (columnModels[j] == null) {
          continue;
        }
        double value = columnModels[j].get(i);
        V.set(j, i, value);
        norm = Utils.hypot(norm, value);
      }
      for (int j = 0; j < origDimension; j++) {
        V.set(j, i, norm == 0.0 ? 0.0 : V.get(j, i) / norm);
      }
      R.set(i, i, norm);
    }
    //System.err.println(R);
    isUpdated = false;
    return V;
  }
  
  @Override
  public Matrix getUSi(SparseVector ui) {
    if (isUpdated) {
      getV();
    }
    if (ui == null) {
      return new Matrix(1, dimension);
    }
    Matrix USi = R.mulLeft(ui);
    return USi;
  }
  
  @Override
  public String toString() {
    return getV().toString();
  }

  @Override
  public LowRankDecomposition getModelPart() {
    return this;
  }
  
  protected SparseVector initVector() {
    double[] newVector = new double[dimension];
    for (int d = 0; d < dimension; d++) {
      newVector[d] = 1.0 / origDimension;
    }
    return new SparseVector(newVector);
  }

}
