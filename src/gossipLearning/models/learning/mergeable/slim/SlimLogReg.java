package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class SlimLogReg extends MergeableLogReg {
  private static final long serialVersionUID = 6140967577949903596L;
  
  /** @hidden */
  private static final String PAR_LAMBDA = "SlimLogReg.lambda";
  private static final String PAR_SIZE = "SlimLogReg.size";
  
  protected final int modelSize;

  public SlimLogReg(String prefix){
    this(prefix, PAR_LAMBDA, PAR_SIZE);
  }
  
  protected SlimLogReg(String prefix, String PAR_LAMBDA, String PAR_SIZE) {
    super(prefix, PAR_LAMBDA);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  protected SlimLogReg(SlimLogReg a){
    super(a);
    modelSize = a.modelSize;
  }
  
  public Object clone(){
    return new SlimLogReg(this);
  }
  
  @Override
  public SlimLogReg merge(final MergeableLogReg model) {
    super.merge(model);
    return this;
  }
  
  /*@Override
  public void update(InstanceHolder instances) {
    int idx = CommonState.r.nextInt(instances.size());
    SparseVector instance = instances.getInstance(idx);
    double label = instances.getLabel(idx);
    super.update(instance, label);
  }*/

  @Override
  public SlimLogReg getModelPart() {
    // 1 draw: v/sum prob to select
    // 1 draw: 1 - v/sum prob to not select
    // k draw: (1 - v/sum)^k prob to not select
    // k draw: 1 - (1 - v/sum)^k prob to select
    
    double prob;
    double sum = gradient.norm1();
    SlimLogReg result = new SlimLogReg(this);
    result.w.clear();
    for (VectorEntry e : gradient) {
      // proportional
      prob = Math.abs(e.value) / sum;
      // uniform
      //prob = 1.0 / numberOfFeatures;
      prob = Math.exp(modelSize * Math.log(1.0 - prob));
      prob = 1.0 - prob;
      if (CommonState.r.nextDouble() <= prob) {
        result.w.add(e.index, w.get(e.index));
      }
    }
    return result;
  }

}
