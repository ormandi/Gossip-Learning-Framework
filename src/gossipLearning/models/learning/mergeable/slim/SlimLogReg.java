package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.WeightedRandPerm;

public class SlimLogReg extends MergeableLogReg implements SlimModel {
  private static final long serialVersionUID = 6140967577949903596L;
  
  private static final String PAR_SIZE = "size";
  private static final String PAR_WEIGHTED = "weighted";
  
  protected final int modelSize;
  protected final boolean weighted;
  
  public SlimLogReg(String prefix){
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
    weighted = 1 == Configuration.getInt(prefix + "." + PAR_WEIGHTED);
  }
  
  protected SlimLogReg(SlimLogReg a){
    super(a);
    modelSize = a.modelSize;
    weighted = a.weighted;
  }
  
  public SlimLogReg clone(){
    return new SlimLogReg(this);
  }
  
  @Override
  public Model merge(Model model) {
    SlimLogReg m = (SlimLogReg)model;
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
    SlimLogReg result = new SlimLogReg(this);
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
    if (!weighted) {
      // TODO: remove this by scaling times of weighted
      result.w.mul(numberOfFeatures / (double)Math.abs(modelSize));
    }
    return result;
  }
  
  private double biasWeight = 0.0;
  private SparseVector weight;
  @Override
  public Model weightedAdd(Model model, double times) {
    if (!weighted) {
      super.add(model, times);
      return this;
    }
    if (weight == null) {
      weight = new SparseVector();
    } else {
      // if the w initialization is not 0 do not clean
      w.pointMul(weight);
      weight.mul(biasWeight);
    }
    super.add(model, times);
    SlimLogReg m = (SlimLogReg)model;
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
