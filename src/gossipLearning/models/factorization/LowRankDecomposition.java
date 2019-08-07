package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Arrays;

import peersim.config.Configuration;

public class LowRankDecomposition implements MatrixBasedModel {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "dimension";
  private static final String PAR_K = "k";
  private static final String PAR_ETA = "eta";
  
  protected double age;
  protected final double[][] columnModels;
  // size of the reduced dimensions
  protected final int k;
  // learning rate
  protected final double eta;
  // size of the original dimension
  protected final int dimension;
  protected Matrix R;
  protected Matrix V;
  
  protected boolean isUpdated = true;
  
  public LowRankDecomposition(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    k = Configuration.getInt(prefix + "." + PAR_K);
    eta = Configuration.getDouble(prefix + "." + PAR_ETA);
    age = 0.0;
    columnModels = new double[dimension][];
  }
  
  public LowRankDecomposition(LowRankDecomposition a) {
    age = a.age;
    k = a.k;
    eta = a.eta;
    dimension = a.dimension;
    columnModels = new double[a.columnModels.length][];
    for (int i = 0; i < columnModels.length; i++) {
      if (a.columnModels[i] == null) {
        continue;
      }
      columnModels[i] = a.columnModels[i].clone();
    }
    isUpdated = true;
  }
  
  @Override
  public LowRankDecomposition clone() {
    return new LowRankDecomposition(this);
  }
  
  @Override
  public double[] update(double[] rowModel, SparseVector instance) {
    // rowIndex - userID
    // rowModel - userModel
    // instance - row of the matrix
    age ++;
    double value = 0.0;
    
    // copy values only
    if (rowModel == null) {
      rowModel = initVector(true);
    }
    double[] result = Arrays.copyOf(rowModel, rowModel.length);
    
    for (int i = 0; i < dimension; i++) {
      value = instance.get(i);
      if (columnModels[i] == null) {
        columnModels[i] = initVector(false);
      }
      for (int j = 0; j < k; j++) {
        // get the prediction and the error
        double r = rowModel[j];
        double c = columnModels[i][j];
        double prediction = r * c;
        double error = value - prediction;
        
        // update models
        result[j] += c * eta * error / dimension;
        columnModels[i][j] += r * eta * error;
        
        // deflate the value of the matrix
        value -= prediction;
      }
    }
    isUpdated = true;
    
    // return new user-model
    return result;
  }
  
  @Override
  public double predict(double[] rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    //SparseVector itemModel = columnModels.get(columnIndex);
    double result = Utils.mul(rowModel, columnModels[columnIndex]);
    return result;
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public void setAge(double age) {
    this.age = age;
  }
  
  public InstanceHolder extract(InstanceHolder instances) {
    InstanceHolder result = new InstanceHolder(instances.getNumberOfClasses(), dimension);
    for (int i = 0; i < instances.size(); i++) {
      result.add(extract(instances.getInstance(i)), instances.getLabel(i));
    }
    return result;
  }
  
  public int getK() {
    return k;
  }
  
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
      R = new Matrix(k, k);
      V = new Matrix(dimension, k);
    }
    for (int i = 0; i < k; i++) {
      double norm = 0.0;
      for (int j = 0; j < dimension; j++) {
        if (columnModels[j] == null) {
          columnModels[j] = initVector(false);
        }
        
        double value = columnModels[j][i];
        V.set(j, i, value);
        norm = Utils.hypot(norm, value);
      }
      for (int j = 0; j < dimension; j++) {
        V.set(j, i, norm == 0.0 ? 0.0 : V.get(j, i) / norm);
      }
      R.set(i, i, norm);
    }
    //System.err.println(R);
    isUpdated = false;
    return V;
  }
  
  @Override
  public Matrix getUSi(double[] ui) {
    if (isUpdated) {
      getV();
    }
    if (ui == null) {
      ui = initVector(false);
    }
    Matrix USi = R.mulLeft(new SparseVector(ui));
    return USi;
  }
  
  @Override
  public String toString() {
    return getV().toString();
  }
  
  @Override
  public void clear() {
    for (int i = 0; i < columnModels.length; i++) {
      columnModels[i] = null;
    }
  }
  
  protected double[] initVector(boolean isRow) {
    double[] result = new double[k];
    for (int i = 0; i < k; i++) {
      result[i] = 1.0 / Math.sqrt(k);
    }
    return result;
  }
  
  @Override
  public Model set(Model model) {
    LowRankDecomposition m = (LowRankDecomposition)model;
    age = m.age;
    for (int i = 0; i < columnModels.length; i++) {
      if (m.columnModels[i] == null) {
        columnModels[i] = null;
      } else {
        System.arraycopy(m.columnModels[i], 0, columnModels[i], 0, m.columnModels[i].length);
      }
    }
    return this;
  }

}
