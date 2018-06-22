package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeablePerceptron;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimPerceptron extends MergeablePerceptron {
  private static final long serialVersionUID = -7462501717472741554L;
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  protected final Random r;
  
  public SlimPerceptron(String prefix) {
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
    r = new Random(0);
  }
  
  public SlimPerceptron(SlimPerceptron a) {
    super(a);
    modelSize = a.modelSize;
    r = a.r;
  }
  
  @Override
  public Object clone() {
    return new SlimPerceptron(this);
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
    WeightedRandPerm rp = new WeightedRandPerm(r, weights);
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
