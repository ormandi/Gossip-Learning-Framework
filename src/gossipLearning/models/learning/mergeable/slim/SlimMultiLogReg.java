package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeableMultiLogReg;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimMultiLogReg extends MergeableMultiLogReg {
  private static final long serialVersionUID = 2834866979500268161L;
  
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  protected final Random r;
  
  /**
   * Default constructor that calls the super();
   */
  public SlimMultiLogReg(String prefix) {
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
    r = new Random(0);
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  public SlimMultiLogReg(SlimMultiLogReg a) {
    super(a);
    modelSize = a.modelSize;
    r = a.r;
  }
  
  public Object clone() {
    return new SlimMultiLogReg(this);
  }
  
  @Override
  public SlimMultiLogReg getModelPart() {
    SlimMultiLogReg result = new SlimMultiLogReg(this);
    for (int i = 0; i < numberOfClasses - 1; i++) {
      result.w[i].clear();
      if (gradients[i].size() == 0) {
        continue;
      }
      double[] weights = new double[gradients[i].size()];
      for (int j = 0; j < gradients[i].size(); j++) {
        weights[j] = modelSize < 0 ? 1.0 : Math.abs(gradients[i].valueAt(j));
      }
      WeightedRandPerm rp = new WeightedRandPerm(r, weights);
      rp.reset(gradients[i].size());
      int iter = Math.abs(modelSize);
      while (0 < iter && rp.hasNext()) {
        iter --;
        int idx = gradients[i].indexAt(rp.next());
        result.w[i].add(idx, w[i].get(idx));
      }
    }
    return result;
  }

}
