package gossipLearning.models.learning.privacy;

import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.learning.multiclass.OneVsAllMetaClassifier;
import gossipLearning.utils.SparseVector;

import java.util.Random;

public class PrivateOvsA extends OneVsAllMetaClassifier implements PrivateModel {
  private static final long serialVersionUID = 3977109418245000412L;

  private static final String PAR_BNAME = "PrivateOvsA";
  
  public PrivateOvsA(String prefix) {
    super(prefix, PAR_BNAME);
  }
  
  public PrivateOvsA(PrivateOvsA a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new PrivateOvsA(this);
  }

  @Override
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r) {
    age ++;
    for (int i = 0; i < numberOfClasses; i++) {
      ((PrivateModel)classifiers.getModel(i)).update(instance, (label == i) ? 1.0 : 0.0, budgetProportion, eps, numFeatures, r);
    }
  }
  
  /*@Override
  public void update(InstanceHolder instances, double budgetProportion, double eps, Random r) {
    double[] labels = new double[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      labels[i] = instances.getLabel(i);
    }
    for (int i = 0; i < numberOfClasses; i++) {
      for (int j = 0; j < instances.size(); j++) {
        instances.setLabel(j, labels[j] == i ? 1.0 : 0.0);
      }
      ((PrivateModel)classifiers.getModel(i)).update(instances, budgetProportion, eps, r);
    }
    for (int i = 0; i < instances.size(); i++) {
      instances.setLabel(i, labels[i]);
    }
    labels = null;
  }*/

}
