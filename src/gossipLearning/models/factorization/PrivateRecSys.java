package gossipLearning.models.factorization;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Random;

import peersim.core.CommonState;

public class PrivateRecSys extends RecSysNormedModel {
  private static final long serialVersionUID = 2162557238372224393L;
  private static final String PAR_DIMENSION = "PrivateRecSys.dimension";
  private static final String PAR_LAMBDA = "PrivateRecSys.lambda";
  private static final String PAR_ALPHA = "PrivateRecSys.alpha";
  private static final String PAR_MIN = "PrivateRecSys.min";
  private static final String PAR_MAX = "PrivateRecSys.max";
  private static final String PAR_NUMITEMS = "PrivateRecSys.origdim";
  protected final double[] noise;
  
  public PrivateRecSys(String prefix) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_MIN, PAR_MAX, PAR_NUMITEMS);
    noise = new double[dimension + 1];
  }
  
  public PrivateRecSys(String prefix, String PAR_DIMENSION, String PAR_LAMBDA, String PAR_ALPHA) {
    super(prefix, PAR_DIMENSION, PAR_LAMBDA, PAR_ALPHA, PAR_MIN, PAR_MAX, PAR_NUMITEMS);
    noise = new double[dimension + 1];
  }
  
  public PrivateRecSys(PrivateRecSys a) {
    super(a);
    noise = new double[dimension + 1];
    System.arraycopy(a.noise, 0, noise, 0, dimension + 1);
  }
  
  public Object clone() {
    return new PrivateRecSys(this);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance, double budgetProportion, double eps, Random r) {
    return this.update(rowIndex, rowModel, instance, budgetProportion, eps, r, true);
  }
  
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance, double budgetProportion, double eps, Random r, boolean updY) {
    double[] newVector;
    if (rowModel == null) {
      // initialize user-model if its null by uniform random numbers  on [0,1]
      newVector = new double[dimension + 1];
      for (int i = 0; i < dimension; i++) {
        //newVector[i] = CommonState.r.nextDouble() / dimension;
        newVector[i] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;
      }
      newVector[dimension] = instance.sum() / instance.size();
      rowModel = new SparseVector(newVector);
      rowModel.normalize();
    }
    /*if (columnModels.size() == 0) {
      for (int i = 0; i < numItems; i++) {
        // initialize item-model is its null by uniform random numbers on [0,1]
        newVector = new double[dimension + 1];
        for (int d = 0; d < dimension; d++) {
          //newVector[i] = CommonState.r.nextDouble() / dimension;
          newVector[d] = CommonState.r.nextDouble() * Math.sqrt(2.0*maxRating) / dimension;
        }
        newVector[dimension] = 1.0;
        SparseVector itemModel = new SparseVector(newVector);
        itemModel.normalize();
        columnModels.put(i, itemModel);
      }
    }*/
    age ++;
    
    //SparseVector copy = new SparseVector(rowModel);
    for (VectorEntry e : instance) {
      SparseVector itemModel = columnModels[e.index];
      if (itemModel == null) {
        throw new RuntimeException("null itemModel!!!");
      }
      // get the prediction and the error
      double prediction = itemModel.mul(rowModel);
      double expected = (2.0 * e.value - minRating - maxRating)/(maxRating - minRating);
      double error = expected - prediction;
      //System.out.println(prediction + "\t" + expected + "\t" + error);
      
      // update models
      double bias = rowModel.get(dimension);
      rowModel.mul(1.0 - alpha);
      rowModel.add(dimension, bias - rowModel.get(dimension));
      rowModel.add(itemModel, lambda * error);
      
      if (updY) {
        bias = itemModel.get(dimension);
        itemModel.mul(1.0 - alpha);
        itemModel.add(rowModel, lambda * error);
        itemModel.add(dimension, bias - itemModel.get(dimension));
      }
    }
    // TODO: add random noise to all of itemModels and normalize!!!
    if (updY) {
      double length;
      for (int i = 0; i < origDimension; i++) {
        length = 0.0;
        double lap = Utils.nextLaplace(0.0, 1.0, r);
        for (int d = 0; d < noise.length; d++) {
          noise[d] = r.nextGaussian();
          length = Utils.hypot(length, noise[d]);
        }
        for (int d = 0; d < noise.length; d++) {
          noise[d] /= length / lap;
          noise[d] *= lambda / (eps * budgetProportion);
        }
        SparseVector itemModel = columnModels[i];
        itemModel.add(new SparseVector(noise), lambda * 4.0);
        itemModel.normalize();
      }
    }
    // return new user-model
    rowModel.normalize();
    return rowModel;
  }
  
  /*@Override
  public RecSysModel getModelPart(Set<Integer> indices) {
    return new RecSysModel(this);
  }*/
  
  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    // rowIndex - userID
    // rowModel - userModel
    // columnIndex - itemID
    SparseVector itemModel = columnModels[columnIndex];
    double predicted = 0.0;
    if (rowModel == null) {
      predicted = 0.0;
    } else if (itemModel == null) {
      predicted = rowModel.get(dimension);
    } else {
      predicted = itemModel.mul(rowModel);
    }
    double ret = 0.5 * (predicted * (maxRating - minRating) + maxRating + minRating);
    //System.out.println(predicted + "\t" + ret);
    return ret;
  }
  
}
