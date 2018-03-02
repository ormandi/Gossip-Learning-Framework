package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeablePegasos;
import gossipLearning.utils.VectorEntry;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class SlimPegasos extends MergeablePegasos {
  private static final long serialVersionUID = 6849809999453437967L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "SlimPegasos.lambda";
  protected static final String PAR_SIZE = "SlimPegasos.size";
  
  protected final int modelSize;
  
  public SlimPegasos(String prefix){
    super(prefix, PAR_LAMBDA);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected SlimPegasos(SlimPegasos a){
    super(a);
    modelSize = a.modelSize;
  }
  
  public Object clone(){
    return new SlimPegasos(this);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public SlimPegasos merge(MergeablePegasos model) {
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
  public SlimPegasos getModelPart() {
    double prob;
    double sum = gradient.norm1();
    SlimPegasos result = new SlimPegasos(this);
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
    
    /*double sum = gradient.norm1();
    //double sum = gradient.size();
    double[] probs = new double[modelSize];
    for (int i = 0; i < probs.length; i++) {
      probs[i] = CommonState.r.nextDouble() * sum;
    }
    Arrays.sort(probs);
    sum = 0.0;
    int idx = 0;
    
    SlimPegasos result = new SlimPegasos(this);
    result.w.clear();
    
    for (VectorEntry e : gradient) {
      sum += Math.abs(e.value);
      //sum += 1.0;
      if (probs[idx] <= sum) {
        result.w.add(e.index, this.w.get(e.index));
      }
      while (idx < probs.length && probs[idx] <= sum) {
        idx ++;
      }
      if (probs.length <= idx) {
        break;
      }
    }*/
    //SparseVector w = new SparseVector(probs.length);
    /*for (int index : indices) {
      w.add(index, this.w.get(index));
    }*/
    return result;
  }

}
