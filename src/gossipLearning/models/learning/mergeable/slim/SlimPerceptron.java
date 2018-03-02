package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeablePerceptron;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class SlimPerceptron extends MergeablePerceptron {
  private static final long serialVersionUID = -7462501717472741554L;
  protected static final String PAR_LAMBDA = "SlimPerceptron.lambda";
  protected static final String PAR_AFUNC = "SlimPerceptron.activation";
  protected static final String PAR_GFUNC = "SlimPerceptron.gradient";
  private static final String PAR_SIZE = "SlimPerceptron.size";
  
  protected final int modelSize;
  
  public SlimPerceptron(String prefix) {
    super(prefix, PAR_LAMBDA, PAR_AFUNC, PAR_GFUNC);
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
  public SlimPerceptron merge(MergeablePerceptron a) {
    super.merge(a);
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
  public SlimPerceptron getModelPart() {
    double prob;
    double sum = gradient.norm1();
    SlimPerceptron result = new SlimPerceptron(this);
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
