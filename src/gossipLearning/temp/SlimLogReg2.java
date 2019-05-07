package gossipLearning.temp;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.WeightedRandPerm;

public class SlimLogReg2 extends MergeableLogReg implements SlimModel {
  private static final long serialVersionUID = 6140967577949903596L;
  
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  
  public SlimLogReg2(String prefix){
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  protected SlimLogReg2(SlimLogReg2 a){
    super(a);
    modelSize = a.modelSize;
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
    result.w.clear();
    if (gradient.size() == 0) {
      return result;
    }
    double[] weights = new double[gradient.size()];
    for (int i = 0; i < gradient.size(); i++) {
      weights[i] = modelSize < 0 ? 1.0 : Math.abs(gradient.valueAt(i));
    }
    WeightedRandPerm rp = new WeightedRandPerm(CommonState.r, weights);
    rp.reset(gradient.size());
    int iter = Math.abs(modelSize);
    while (0 < iter && rp.hasNext()) {
      iter --;
      int idx = gradient.indexAt(rp.next());
      result.w.add(idx, w.get(idx));
    }
    return result;
  }
  
  
  public Model weightedAdd(Model model, double times) {
    return add(model, times);
  }
  private double biasWeight = 0.0;
  private SparseVector weight;
  @Override
  public Model add(Model model, double times) {
    if (weight == null) {
      weight = new SparseVector();
    } else {
      // if the w initialization is not 0 do not clean
      for (VectorEntry e : weight) {
        w.put(e.index, w.get(e.index) * e.value);
      }
      weight.mul(biasWeight);
    }
    super.add(model, times);
    SlimLogReg2 m = (SlimLogReg2)model;
    biasWeight += times;
    for (VectorEntry entry : m.w) {
      weight.add(entry.index, times);
    }
    weight.mul(1.0 / biasWeight);
    for (VectorEntry e : weight) {
      w.put(e.index, w.get(e.index) / e.value);
    }
    return this;
  }
  
  @Override
  public void clear() {
    super.clear();
    weight = null;
    biasWeight = 0.0;
  }

}
