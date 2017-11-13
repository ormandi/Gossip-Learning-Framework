package gossipLearning.models.learning.privacy;

import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.learning.multiclass.MultiLogReg;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Random;

public class PrivateMultiLogreg extends MultiLogReg implements PrivateModel {
  private static final long serialVersionUID = -8947684933095643280L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "PrivateMultiLogreg.lambda";
  
  protected double[] noise;
  
  public PrivateMultiLogreg(String prefix) {
    super(prefix, PAR_LAMBDA);
  }
  
  public PrivateMultiLogreg(PrivateMultiLogreg a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new PrivateMultiLogreg(this);
  }
  
  @Override
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r) {
    super.update(instance, label);
    
    double nu = 1.0 / (lambda * age);
    
    // add noise to each classes
    for (int i = 0; i < numberOfClasses -1; i++) {
      if (noise == null) noise = new double[(int)numFeatures];
      for (int d = 0; d < numFeatures; d++) {
        noise[d] = Utils.nextLaplace(0.0, 1.0, r);
      }
      
      double sensitivity = 2.0;
      double scale = sensitivity * nu / (eps * budgetProportion);
      /*if (isAdaptive) {
        sensitivity = Math.max(Math.abs(-1-err), Math.abs(1-err));
      }*/
      w[i].add(noise, scale);
    }
  }
  
  @Override
  public void update(InstanceHolder instances, double budgetProportion, double eps, Random r) {
    super.update(instances);
    
    double nu = 1.0 / (lambda * age);
    
    int numFeatures = instances.getNumberOfFeatures();
    // add noise to each classes
    for (int i = 0; i < numberOfClasses -1; i++) {
      if (noise == null) noise = new double[(int)numFeatures];
      for (int d = 0; d < numFeatures; d++) {
        noise[d] = Utils.nextLaplace(0.0, 1.0, r);
      }
      
      double sensitivity = 2.0;
      double scale = sensitivity * nu / (eps * budgetProportion);
      w[i].add(noise, scale / instances.size());
    }
  }

}
