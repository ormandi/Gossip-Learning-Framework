package gossipLearning.temp;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class SlimLogReg2 extends MergeableLogReg implements SlimModel {
  private static final long serialVersionUID = 6140967577949903596L;
  
  private static final String PAR_PERCENT = "percent";
  
  protected final double percent;
  protected int[] indices;
  
  public SlimLogReg2(String prefix){
    super(prefix);
    percent = Configuration.getDouble(prefix + "." + PAR_PERCENT);
  }
  
  protected SlimLogReg2(SlimLogReg2 a){
    super(a);
    percent = a.percent;
  }
  
  public Object clone(){
    return new SlimLogReg2(this);
  }
  
  @Override
  public Model merge(Model model) {
    SlimLogReg2 m = (SlimLogReg2)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (VectorEntry e : m.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * modelWeight);
    }
    bias += (m.bias - bias) * modelWeight;
    return this;
  }
  
  @Override
  public Model getModelPart() {
    SlimLogReg2 result = new SlimLogReg2(this);
    if (indices == null) {
      indices = new int[numberOfFeatures];
      for (int i = 0; i < numberOfFeatures; i++) {
        indices[i] = i;
      }
    }
    result.w.clear();
    Utils.arrayShuffle(CommonState.r, indices);
    int size = (int)Math.floor(percent * numberOfFeatures);
    size += CommonState.r.nextDouble() < (percent * numberOfFeatures) - size ? 1 : 0;
    for (int i = 0; i < size; i++) {
      int idx = indices[i];
      result.w.add(idx, w.get(idx));
    }
    return result;
  }
  
  private double biasWeight = 0.0;
  private SparseVector weight;
  @Override
  public Model weightedAdd(Model model, double times) {
    if (weight == null) {
      weight = new SparseVector();
    } else {
      // if the w initialization is not 0 do not clean
      w.pointMul(weight);
      weight.mul(biasWeight);
    }
    super.add(model, times);
    SlimLogReg2 m = (SlimLogReg2)model;
    biasWeight += times;
    for (VectorEntry entry : m.w) {
      weight.add(entry.index, times);
    }
    weight.mul(1.0 / biasWeight);
    w.div(weight);
    return this;
  }
  
  @Override
  public void clear() {
    super.clear();
    weight = null;
    biasWeight = 0.0;
  }

}
