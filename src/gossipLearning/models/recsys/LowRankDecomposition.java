package gossipLearning.models.recsys;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class LowRankDecomposition implements MatrixBasedModel, FeatureExtractor, Mergeable<LowRankDecomposition> {
  private static final long serialVersionUID = -6695974880876825151L;
  private static final String PAR_DIMENSION = "LowRankDecomposition.dimension";
  private static final String PAR_LAMBDA = "LowRankDecomposition.lambda";
  private static final String PAR_ALPHA = "LowRankDecomposition.alpha";
  
  protected double age;
  protected HashMap<Integer, SparseVector> itemModels;
  protected int dimension;
  // learning rate
  protected double lambda;
  // regularization parameter
  protected double alpha;
  protected int maxindex;
  
  public LowRankDecomposition() {
    age = 0.0;
    itemModels = new HashMap<Integer, SparseVector>();
    dimension = 10;
    lambda = 0.001;
    alpha = 0.0;
    maxindex = 0;
  }
  
  public LowRankDecomposition(LowRankDecomposition a) {
    age = a.age;
    itemModels = new HashMap<Integer, SparseVector>();
    for (Entry<Integer, SparseVector> e : a.itemModels.entrySet()) {
      itemModels.put(e.getKey(), (SparseVector)e.getValue().clone());
    }
    dimension = a.dimension;
    lambda = a.lambda;
    alpha = a.alpha;
    maxindex = a.maxindex;
  }
  
  public Object clone() {
    return new LowRankDecomposition(this);
  }
  
  @Override
  public void init(String prefix) {
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
  }

  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    // rowIndex - userID
    // rowModel - userModel
    // instance - userRatings
    double[] newVector;
    if (rowModel == null) {
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = CommonState.r.nextDouble();
      }
      rowModel = new SparseVector(newVector);
    }
    if (maxindex < instance.maxIndex()) {
      maxindex = instance.maxIndex();
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
    for (int index = 0; index <= maxindex; index++){
      value = 0.0;
      if (entry != null && entry.index == index) {
        value = entry.value;
        if (iterator.hasNext()) {
          entry = iterator.next();
        } else {
          entry = null;
        }
      }
      SparseVector itemModel = itemModels.get(index);
      // initialize a new item-model by uniform random numbers [0,1]
      if (itemModel == null) {
        newVector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          newVector[i] = CommonState.r.nextDouble();
        }
        itemModel = new SparseVector(newVector);
        itemModels.put(index, itemModel);
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
  
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    SparseVector itemModel = itemModels.get(columnIndex);
    if (itemModel == null || rowModel == null) {
      return 0.0;
    }
    return itemModel.mul(rowModel);
  }

  @Override
  public double getAge() {
    return age;
  }
  
  public LowRankDecomposition getModelPart(Set<Integer> indices) {
    LowRankDecomposition result = new LowRankDecomposition();
    result.age = age;
    result.dimension = dimension;
    result.lambda = lambda;
    result.alpha = alpha;
    result.maxindex = maxindex;
    for (int index : indices) {
      SparseVector v = itemModels.get(index);
      if (v != null) {
        result.itemModels.put(index, v);
      }
    }
    return result;
  }

  @Override
  public LowRankDecomposition merge(LowRankDecomposition model) {
    for (Entry<Integer, SparseVector> e : model.itemModels.entrySet()) {
      itemModels.put(e.getKey(), e.getValue());
    }
    return this;
  }
  
  public void setDimension(int dimension) {
    this.dimension = dimension;
  }
  
  public InstanceHolder extract(InstanceHolder instances) {
    InstanceHolder result = new InstanceHolder(instances.getNumberOfClasses(), dimension);
    for (int i = 0; i < instances.size(); i++) {
      result.add(extract(instances.getInstance(i)), instances.getLabel(i));
    }
    return result;
  }
  
  public SparseVector extract(SparseVector instance) {
    SparseVector result = new SparseVector(dimension);
    for (Entry<Integer, SparseVector> e : itemModels.entrySet()) {
      double value = instance.get(e.getKey());
      for (int i = 0; i < dimension; i++) {
        double mvalue = e.getValue().get(i);
        result.add(i, value * mvalue);
      }
    }
    return result;
  }
  
  public String toString() {
    return itemModels.toString();
  }
  
  public SparseVector[] getVectors() {
    SparseVector[] result = new SparseVector[dimension];
    for (int i = 0; i < dimension; i++) {
      result[i] = new SparseVector();
      for (Entry<Integer, SparseVector> e : itemModels.entrySet()) {
        result[i].put(e.getKey(), e.getValue().get(i));
      }
    }
    return result;
  }

}
