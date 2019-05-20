package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;



public class SlimRecSys extends MergeableRecSys implements SlimModel {
  private static final long serialVersionUID = -2675295901691742814L;
  private static final String PAR_SIZE = "size";
  
  protected final double modelSize;
  protected final int[] indices;
  protected int indSize;
  protected final double[] weights;
  
  public SlimRecSys(String prefix) {
    super(prefix);
    modelSize = Configuration.getDouble(prefix + "." + PAR_SIZE);
    indices = new int[dimension];
    weights = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      indices[i] = i;
      weights[i] = 0.0;
    }
  }
  
  public SlimRecSys(SlimRecSys a) {
    super(a);
    modelSize = a.modelSize;
    indices = a.indices.clone();
    indSize = a.indSize;
    weights = a.weights.clone();
  }
  
  public Object clone() {
    return new SlimRecSys(this);
  }
  
  @Override
  public double[] update(double[] rowModel, SparseVector instance) {
    // Mark's permutation class can be used
    indSize = instance.size();
    int prev = 0;
    int preIdx = 0;
    int postIdx = indSize;
    for (VectorEntry entry : instance) {
      weights[entry.index] ++;
      for (int i = prev; i < entry.index; i++) {
        indices[postIdx] = i;
        postIdx ++;
      }
      indices[preIdx] = entry.index;
      preIdx ++;
      prev = entry.index + 1;
    }
    for (int i = prev; i < dimension; i++) {
      indices[postIdx] = i;
      postIdx ++;
    }
    return super.update(rowModel, instance);
  }
  
  @Override
  public SlimRecSys getModelPart() {
    SlimRecSys result = new SlimRecSys(this);
    Utils.arrayShuffle(CommonState.r, indices, 0, indSize);
    Utils.arrayShuffle(CommonState.r, indices, indSize, indices.length);
    double tempSize = dimension * modelSize;
    int floorSize = (int)Math.floor(tempSize);
    int partSize = floorSize + (CommonState.r.nextDouble() < tempSize - floorSize ? 1 : 0);
    for (int i = partSize; i < indices.length; i++) {
      result.columnModels[indices[i]] = null;
    }
    return result;
  }
  
  @Override
  public Model merge(Model model) {
    SlimRecSys m = (SlimRecSys)model;
    age = Math.max(age, m.age);
    for (int i = 0; i < dimension; i++) {
      double modelWeight = m.weights[i] / (weights[i] + m.weights[i]);
      if (m.columnModels[i] == null) {
        continue;
      } else if (columnModels[i] == null) {
        columnModels[i] = m.columnModels[i].clone();
      } else {
        for (int j = 0; j < columnModels[i].length; j++) {
          //columnModels[i][j] = (weights[i] * columnModels[i][j]) + (m.weights[i] * m.columnModels[i][j]);
          //columnModels[i][j] /= (weights[i] + m.weights[i]);
          columnModels[i][j] += (m.columnModels[i][j] - columnModels[i][j]) * modelWeight;
        }
      }
      weights[i] = Math.max(weights[i], m.weights[i]);
    }
    return this;
  }

  @Override
  public Model weightedAdd(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Matrix getV() {
    throw new RuntimeException("Function getV should not be called!");
  }

}
