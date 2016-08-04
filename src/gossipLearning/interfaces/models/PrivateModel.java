package gossipLearning.interfaces.models;

import gossipLearning.utils.SparseVector;

import java.util.Random;

public interface PrivateModel extends LearningModel {
  /**
   * This method updates the actual model with a training instance.
   * @param instance the features that represent the instance
   * @param label the class label of the training instance or Double.NaN in case of clustering
   * @param budgetProportion the proportion of the privacy budget will be used
   */
  public void update(SparseVector instance, double label, double budgetProportion, double eps, double numFeatures, Random r);
  //public void update(InstanceHolder instances, double budgetProportion, double eps, Random r);
}
