package gossipLearning.models.recsys;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class LowRankDecomposition implements MatrixBasedModel, FeatureExtractor {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "LowRankDecomposition.dimension";
  private static final String PAR_LAMBDA = "LowRankDecomposition.lambda";
  private static final String PAR_ALPHA = "LowRankDecomposition.alpha";
  
  protected double age;
  protected HashMap<Integer, SparseVector> columnModels;
  protected int dimension;
  // learning rate
  protected double lambda;
  // regularization parameter
  protected double alpha;
  protected int maxIndex;
  
  public LowRankDecomposition() {
    age = 0.0;
    columnModels = new HashMap<Integer, SparseVector>();
    dimension = 10;
    lambda = 0.001;
    alpha = 0.0;
    maxIndex = 0;
  }
  
  public LowRankDecomposition(LowRankDecomposition a) {
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
    dimension = a.dimension;
    lambda = a.lambda;
    alpha = a.alpha;
    maxIndex = a.maxIndex;
  }
  
  public LowRankDecomposition(double age, HashMap<Integer, SparseVector> columnModels, int dimension, double lambda, double alpha, int maxIndex) {
    this.age = age;
    this.columnModels = columnModels;
    this.dimension = dimension;
    this.lambda = lambda;
    this.alpha = alpha;
    this.maxIndex = maxIndex;
  }
  
  @Override
  public Object clone() {
    return new LowRankDecomposition(this);
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
    double[] newVector;
    if (rowModel == null) {
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = CommonState.r.nextDouble();
      }
      rowModel = new SparseVector(newVector);
    }
    if (maxIndex < instance.maxIndex()) {
      maxIndex = instance.maxIndex();
    }
    
    SparseVector newUserModel = (SparseVector)rowModel.clone();
    age ++;
    
    newUserModel.mul(1.0 - alpha);
    double value = 0.0;
    VectorEntry entry = null;
    Iterator<VectorEntry> iterator = instance.iterator();
    if (iterator.hasNext()) {
      entry = iterator.next();
    }
    for (int index = 0; index <= maxIndex; index++){
      value = 0.0;
      if (entry != null && entry.index == index) {
        value = entry.value;
        if (iterator.hasNext()) {
          entry = iterator.next();
        } else {
          entry = null;
        }
      }
      SparseVector itemModel = columnModels.get(index);
      // initialize a new item-model by uniform random numbers [0,1]
      if (itemModel == null) {
        newVector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          newVector[i] = CommonState.r.nextDouble();
        }
        itemModel = new SparseVector(newVector);
        columnModels.put(index, itemModel);
      }
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double error = value - prediction;
      
      // update models
      newUserModel.add(itemModel, lambda * error);
      itemModel.mul(1.0 - alpha);
      itemModel.add(rowModel, lambda * error);
    }
    
    // return new user-model
    return newUserModel;
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
  
  public void setDimension(int dimension) {
    this.dimension = dimension;
  }
  
  public SparseVector extract(SparseVector instance) {
    SparseVector result = new SparseVector(dimension);
    for (Entry<Integer, SparseVector> e : columnModels.entrySet()) {
      double value = instance.get(e.getKey());
      for (int i = 0; i < dimension; i++) {
        double mvalue = e.getValue().get(i);
        result.add(i, value * mvalue);
      }
    }
    return result;
  }
  
  public SparseVector[] getVectors() {
    SparseVector[] result = new SparseVector[dimension];
    for (int i = 0; i < dimension; i++) {
      result[i] = new SparseVector();
      for (Entry<Integer, SparseVector> e : columnModels.entrySet()) {
        result[i].put(e.getKey(), e.getValue().get(i));
      }
    }
    return result;
  }
  
  @Override
  public String toString() {
    return columnModels.toString();
  }

}
