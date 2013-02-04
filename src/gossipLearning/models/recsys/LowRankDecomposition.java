package gossipLearning.models.recsys;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class LowRankDecomposition implements Model, Mergeable<LowRankDecomposition> {
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
    lambda = 0.05;
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

  public SparseVector update(SparseVector ratings, SparseVector userModel) {
    double[] newVector;
    if (userModel == null) {
      newVector = new double[dimension];
      for (int i = 0; i < dimension; i++) {
        newVector[i] = CommonState.r.nextDouble();
      }
      userModel = new SparseVector(newVector);
    }
    if (maxindex < ratings.maxIndex()) {
      maxindex = ratings.maxIndex();
    }
    
    SparseVector newUserModel = (SparseVector)userModel.clone();
    age ++;
    
    newUserModel.mul(1.0 - alpha);
    double value = 0.0;
    VectorEntry entry = null;
    Iterator<VectorEntry> iterator = ratings.iterator();
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
      // get the prediction and the error
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
      double prediction = itemModel.mul(userModel);
      double error = value - prediction;
      
      // update models
      newUserModel.add(itemModel, lambda * error);
      itemModel.mul(1.0 - alpha);
      itemModel.add(userModel, lambda * error);
    }
    
    // return new user-model
    return newUserModel;
  }
  
  public double predict(int itemId, SparseVector userModel) {
    SparseVector itemModel = itemModels.get(itemId);
    if (itemModel == null || userModel == null) {
      return 0.0;
    }
    return itemModel.mul(userModel);
  }

  @Override
  public double getAge() {
    return age;
  }
  
  public RecSysModel getModelPart(SparseVector rates, int numRandToGen) {
    RecSysModel result = new RecSysModel();
    result.age = age;
    result.dimension = dimension;
    for (VectorEntry e : rates) {
      SparseVector v = itemModels.get(e.index);
      if (v != null) {
        result.itemModels.put(e.index, v);
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
  
  public SparseVector extract(SparseVector instance) {
    SparseVector result = new SparseVector(itemModels.size());
    for (int i = 0; i < dimension; i++) {
      for (Entry<Integer, SparseVector> e : itemModels.entrySet()) {
        result.add(i, instance.get(e.getKey())*e.getValue().get(i));
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
