package gossipLearning.models.learning;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class CompressedPegasos extends P2Pegasos {
  private static final long serialVersionUID = 970207044267171686L;
  private static final String PAR_NBITS = "nbits";
  
  protected final int nbits;

  public CompressedPegasos(String prefix) {
    super(prefix);
    nbits = Configuration.getInt(prefix + "." + PAR_NBITS);
  }
  
  protected CompressedPegasos(CompressedPegasos a) {
    super(a);
    nbits = a.nbits;
  }
  
  @Override
  public CompressedPegasos clone() {
    return new CompressedPegasos(this);
  }
  
  @Override
  protected void gradient(SparseVector instance, double label) {
    gradient.clear();
    label = (label == 0.0) ? -1.0 : label;
    boolean isSV = label * w.mul(instance) < 1.0;
    if (isSV) {
      gradient.add(instance, -label).scaleValueRange(nbits, CommonState.r);
    }
    gradient.add(w, lambda);
  }
  
  protected SparseVector inst_tmp = new SparseVector();
  @Override
  protected void gradient(InstanceHolder instances) {
    gradient.clear();
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      label = (label == 0.0) ? -1.0 : label;
      boolean isSV = label * w.mul(instance) < 1.0;
      if (isSV) {
        inst_tmp.set(instance).mul(-label).scaleValueRange(nbits, CommonState.r);
        gradient.add(inst_tmp);
      }
    }
    gradient.add(w, lambda * instances.size());
  }

}
