package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.models.learning.mergeable.MergeableLogReg;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimLogReg extends MergeableLogReg {
  private static final long serialVersionUID = 6140967577949903596L;
  
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  protected final Random r;
  
  public SlimLogReg(String prefix){
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
    r = new Random(0);
  }
  
  protected SlimLogReg(SlimLogReg a){
    super(a);
    modelSize = a.modelSize;
    r = a.r;
  }
  
  public Object clone(){
    return new SlimLogReg(this);
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
