package gossipLearning.models.learning.mergeable.slim;

import peersim.config.Configuration;
import peersim.core.CommonState;
import gossipLearning.models.learning.mergeable.MergeableMultiLogReg;
import gossipLearning.utils.VectorEntry;

public class SlimMultiLogReg extends MergeableMultiLogReg {
  private static final long serialVersionUID = 2834866979500268161L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "SlimMultiLogReg.lambda";
  private static final String PAR_SIZE = "SlimMultiLogReg.size";
  
  protected final int modelSize;
  
  /**
   * Default constructor that calls the super();
   */
  public SlimMultiLogReg(String prefix) {
    super(prefix, PAR_LAMBDA);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  public SlimMultiLogReg(SlimMultiLogReg a) {
    super(a);
    modelSize = a.modelSize;
  }
  
  public Object clone() {
    return new SlimMultiLogReg(this);
  }
  
  @Override
  public SlimMultiLogReg merge(MergeableMultiLogReg model) {
    super.merge(model);
    return this;
  }

  @Override
  public SlimMultiLogReg getModelPart() {
    SlimMultiLogReg result = new SlimMultiLogReg(this);
    for (int i = 0; i < numberOfClasses - 1; i++) {
      double prob;
      double sum = result.gradients[i].norm1();
      result.w[i].clear();
      for (VectorEntry e : gradients[i]) {
        // proportional
        prob = Math.abs(e.value) / sum;
        // uniform
        //prob = 1.0 / numberOfFeatures;
        prob = Math.exp(modelSize * Math.log(1.0 - prob));
        prob = 1.0 - prob;
        if (CommonState.r.nextDouble() <= prob) {
          result.w[i].add(e.index, w[i].get(e.index));
        }
      }
    }
    return result;
  }

}
