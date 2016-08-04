package gossipLearning.models.learning.privacy;

import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.learning.P2Pegasos;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Random;

import peersim.config.Configuration;

public class PrivatePegasos extends P2Pegasos implements PrivateModel {
  private static final long serialVersionUID = 1361721163541934819L;

  protected static final String PAR_LAMBDA = "PrivatePegasos.lambda";
  protected static final String PAR_METHOD = "PrivatePegasos.isAdaptive";
  protected static final String PAR_NOISE = "PrivatePegasos.isLaplace";
  
  protected final boolean isAdaptive;
  protected final boolean isLaplace;
  protected double[] noise;
  
  public PrivatePegasos(String prefix) {
    super(prefix, PAR_LAMBDA);
    isAdaptive = Configuration.getBoolean(prefix + "." + PAR_METHOD);
    isLaplace = Configuration.getBoolean(prefix + "." + PAR_NOISE);
  }
  
  public PrivatePegasos(PrivatePegasos a) {
    super(a);
    isAdaptive = a.isAdaptive;
    isLaplace = a.isLaplace;
  }
  
  @Override
  public Object clone() {
    return new PrivatePegasos(this);
  }

  @Override
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r) {
    label = (label == 0.0) ? -1.0 : label;
    age ++;
    double nu = 1.0 / Math.sqrt(age);
    boolean isSV = label * w.mul(instance) < 1.0;
    
    if(noise == null) noise = new double[(int)numFeatures];
    double length = 0.0;
    for (int i = 0; i < numFeatures; i++) {
      if (isLaplace) {
        noise[i] = Utils.nextLaplace(0.0, 1.0, r);
      } else {
        noise[i] = r.nextGaussian();
        length = Utils.hypot(length, noise[i]);
      }
    }
    double lap = isLaplace ? 1.0 : Utils.nextLaplace(0.0, 1.0, r);
    for (int i = 0; i < numFeatures; i++) {
      noise[i] /= isLaplace ? 1.0 : length / lap;
      noise[i] *= nu / (eps * budgetProportion);
    }
    
    w.mul(1.0 - nu * lambda);
    if (isSV) {
      w.add(instance, nu * label);
    }
    
    double sensitivity = 2.0;
    if (isAdaptive && !isSV) {
      sensitivity = 1.0;
    }
    w.add(noise, nu * sensitivity);
  }

}
