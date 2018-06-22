package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeablePegasos;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimPegasos extends MergeablePegasos {
  private static final long serialVersionUID = 6849809999453437967L;
  
  protected static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  protected final Random r;
  
  public SlimPegasos(String prefix){
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
    r = new Random(0);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected SlimPegasos(SlimPegasos a){
    super(a);
    modelSize = a.modelSize;
    r = a.r;
  }
  
  public Object clone(){
    return new SlimPegasos(this);
  }
  
  @Override
  public SlimPegasos getModelPart() {
    SlimPegasos result = new SlimPegasos(this);
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
