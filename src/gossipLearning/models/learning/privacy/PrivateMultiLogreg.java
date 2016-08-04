package gossipLearning.models.learning.privacy;

import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.learning.multiclass.MultiLogReg;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Random;

import peersim.config.Configuration;

public class PrivateMultiLogreg extends MultiLogReg implements PrivateModel {
  private static final long serialVersionUID = -8947684933095643280L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "PrivateMultiLogreg.lambda";
  protected static final String PAR_METHOD = "PrivateMultiLogreg.isAdaptive";
  protected static final String PAR_NOISE = "PrivateMultiLogreg.norm";
  
  protected final boolean isAdaptive;
  protected final String norm;
  protected double[] noise;
  
  public PrivateMultiLogreg(String prefix) {
    super(prefix, PAR_LAMBDA);
    isAdaptive = Configuration.getBoolean(prefix + "." + PAR_METHOD);
    norm = Configuration.getString(prefix + "." + PAR_NOISE);
  }
  
  public PrivateMultiLogreg(PrivateMultiLogreg a) {
    super(a);
    isAdaptive = a.isAdaptive;
    norm = a.norm;
  }
  
  @Override
  public Object clone() {
    return new PrivateMultiLogreg(this);
  }
  
  @Override
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r) {
    age ++;
    double nu = 1.0 / Math.sqrt(age);
    double[] distribution = distributionForInstance(instance);
    
    if (noise == null) noise = new double[(int)numFeatures];
    double length = 1.0;
    double lap = 1.0;
    double bNoise = 0.0;
    double lapValue = 0.0;
    int rIdx = 0;
    for (int d = 0; d < numFeatures; d++) {
      if (norm.equals("L1")) {
        if (d == 0) {
          bNoise = Utils.nextLaplace(0.0, 1.0, r);
        }
        noise[d] = Utils.nextLaplace(0.0, 1.0, r);
      } else if (norm.equals("L2")) {
        if (d == 0) {
          bNoise = r.nextGaussian();
          length = Utils.hypot(length, bNoise);
        }
        noise[d] = r.nextGaussian();
        length = Utils.hypot(length, noise[d]);
      } else if (norm.equals("Linf")) {
        if (d == 0) {
          lapValue = Utils.nextLaplace(0.0, 1.0, r);
          rIdx = r.nextInt(noise.length + 1);
          if (rIdx == noise.length) {
            bNoise = lapValue;
          } else {
            bNoise = r.nextDouble() * lapValue;
          }
        }
        if (d == rIdx) {
          noise[d] = lapValue;
        } else {
          noise[d] = r.nextDouble() * lapValue;
        }
      } else {
        throw new RuntimeException("Unsupported norm: " + norm);
      }
    }
    
    for (int d = 0; d < numFeatures; d++) {
      noise[d] /= length / lap;
      noise[d] *= nu / (eps * budgetProportion);
    }
    bNoise /= length / lap;
    bNoise *= nu / (eps * budgetProportion);
    
    // update for each classes
    for (int i = 0; i < numberOfClasses -1; i++) {
      double cDelta = (label == i) ? 1.0 : 0.0;
      double err = cDelta - distribution[i];
      
      w[i].mul(1.0 - nu * lambda);
      w[i].add(instance, nu * err);
      bias[i] += nu * err;
      
      double sensitivity = 2.0;
      if (isAdaptive) {
        sensitivity = Math.max(Math.abs(-1-err), Math.abs(1-err));
      }
      w[i].add(noise, nu * sensitivity);
      bias[i] += bNoise * nu * sensitivity;
    }
  }

}
