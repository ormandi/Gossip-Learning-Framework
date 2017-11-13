package gossipLearning.models.learning.privacy;

import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.learning.P2Pegasos;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.util.Random;

public class PrivatePegasos extends P2Pegasos implements PrivateModel {
  private static final long serialVersionUID = 1361721163541934819L;

  protected static final String PAR_LAMBDA = "PrivatePegasos.lambda";
  
  protected double[] noise;
  
  public PrivatePegasos(String prefix) {
    super(prefix, PAR_LAMBDA);
  }
  
  public PrivatePegasos(PrivatePegasos a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new PrivatePegasos(this);
  }

  @Override
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r) {
    super.update(instance, label);
    double nu = 1.0 / (lambda * age);
    
    if(noise == null) noise = new double[(int)numFeatures];
    for (int i = 0; i < numFeatures; i++) {
      noise[i] = Utils.nextLaplace(0.0, 1.0, r);
    }
    
    double sensitivity = 2.0;
    /*if (isAdaptive && !isSV) {
      sensitivity = 1.0;
    }*/
    double scale = sensitivity * nu / (eps * budgetProportion);
    w.add(noise, scale);
  }

  @Override
  public void update(InstanceHolder instances, double budgetProportion, double eps, Random r) {
    super.update(instances);
    double nu = 1.0 / (lambda * age);
    
    int numFeatures = instances.getNumberOfFeatures();
    if(noise == null) noise = new double[(int)numFeatures];
    for (int i = 0; i < numFeatures; i++) {
      noise[i] = Utils.nextLaplace(0.0, 1.0, r);
    }
    
    double sensitivity = 2.0;
    double scale = sensitivity * nu / (eps * budgetProportion);
    w.add(noise, scale / instances.size());
  }

}
