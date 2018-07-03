package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeablePerceptron;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.WeightedRandPerm;

public class SlimPerceptron extends MergeablePerceptron implements SlimModel {
  private static final long serialVersionUID = -7462501717472741554L;
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  
  public SlimPerceptron(String prefix) {
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  public SlimPerceptron(SlimPerceptron a) {
    super(a);
    modelSize = a.modelSize;
  }
  
  @Override
  public Object clone() {
    return new SlimPerceptron(this);
  }
  
  @Override
  public Model merge(Model model) {
    SlimPerceptron m = (SlimPerceptron)model;
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
  public SlimPerceptron getModelPart() {
    SlimPerceptron result = new SlimPerceptron(this);
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

}
