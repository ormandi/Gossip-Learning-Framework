package gossipLearning.temp;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.HashMap;
import java.util.Map.Entry;

import peersim.config.Configuration;

public class LowRankYYT implements MatrixBasedModel, FeatureExtractor, Partializable {
  private static final long serialVersionUID = 3547157543302503919L;
  
  private static final String PAR_DIMENSION = "LowRankYYT.dimension";
  private static final String PAR_LAMBDA = "LowRankYYT.lambda";
  private static final String PAR_ALPHA = "LowRankYYT.alpha";
  
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
  
  public LowRankYYT(String prefix) {
    this(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA);
  }
  
  public LowRankYYT(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
    age = 0.0;
    columnModels = new HashMap<Integer, SparseVector>();
    eigenValues = new SparseVector();
    maxIndex = 1;
  }
  
  public LowRankYYT(LowRankYYT a) {
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
  
  public LowRankYYT(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    this.age = age;
    this.columnModels = columnModels;
    this.dimension = dimension;
    this.lambda = lambda;
    this.alpha = alpha;
    this.maxIndex = maxIndex;
  }
  
  @Override
  public Object clone() {
    return new LowRankYYT(this);
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
    //double nu = lambda / Math.log(age + 1);
    double nu = lambda;
    
    double[] newVector;
    
    SparseVector newRowModel = new SparseVector(dimension);
    for (int i = 0; i < dimension; i++) {
      //double ri = rowModel.get(i);
      double ri = 0.0;
      for (int j = 0; j <= maxIndex; j++) {
        SparseVector columnModel = columnModels.get(j);
        if (columnModel == null) {
          newVector = new double[dimension];
          for (int d = 0; d < dimension; d++) {
            newVector[d] = 1.0 / maxIndex;
            //newVector[d] = CommonState.r.nextDouble();
          }
          columnModel = new SparseVector(newVector);
          columnModels.put(j, columnModel);
        }
        
        ri += instance.get(j) * columnModel.get(i);
      }
      newRowModel.put(i, ri);
    }
    //System.out.println(newRowModel);
    
    for (int j = 0; j <= maxIndex; j++) {
      SparseVector columnModel = columnModels.get(j);
      
      value = instance.get(j);
      for (int i = 0; i < dimension; i++) {
        // get the prediction and the error
        double ri = newRowModel.get(i);
        double ci = columnModel.get(i);
        double prediction = ri * ci;
        double error = value - prediction;
        
        // update models
        //newRowModel.add(i, ci * nu * error);
        columnModel.add(i, ri * nu * error);
        
        // deflate the value of the matrix
        value -= prediction;
      }
    }
    
    //TODO: compute corresponding eigenvalue
    
    // set null for normalizer matrix
    R = null;
    
    
    for (int i = 0; i < dimension; i++) {
      double ri = 0.0;
      for (int j = 0; j <= maxIndex; j++) {
        SparseVector columnModel = columnModels.get(j);
        ri += instance.get(j) * columnModel.get(i);
      }
      newRowModel.put(i, ri);
    }
    
    // return new user-model
    return newRowModel;
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

  @Override
  public SparseVector extract(SparseVector instance) {
    if (R == null) {
      getV();
    }
    Matrix res = V.mulLeft(instance);
    SparseVector result = new SparseVector(res.getRow(0));
    return result;
  }

  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    SparseVector itemModel = columnModels.get(columnIndex);
    if (itemModel == null || rowModel == null) {
      return 0.0;
    }
    return itemModel.mul(rowModel);
  }

  @Override
  public Matrix getV() {
    if (R != null) {
      return V;
    }
    R = new Matrix(dimension, dimension);
    V = new Matrix(maxIndex + 1, dimension);
    for (int i = 0; i < dimension; i++) {
      double norm = 0.0;
      for (Entry<Integer, SparseVector> e : columnModels.entrySet()) {
        double value = e.getValue().get(i);
        V.set(e.getKey(), i, value);
        norm = Utils.hypot(norm, value);
      }
      for (int j = 0; j < maxIndex + 1; j++) {
        V.set(j, i, V.get(j, i) / norm);
      }
      R.set(i, i, norm);
    }
    //System.err.println(R);
    return V;
  }

  @Override
  public Matrix getUSi(SparseVector ui) {
    if (R == null) {
      getV();
    }
    if (ui == null) {
      return new Matrix(1, dimension);
    }
    Matrix USi = R.mulLeft(ui);
    return USi;
  }

  @Override
  public LowRankYYT getModelPart() {
    return this;
  }

}
