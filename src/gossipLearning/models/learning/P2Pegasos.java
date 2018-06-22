package gossipLearning.models.learning;

import gossipLearning.interfaces.models.LinearModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

public class P2Pegasos extends LinearModel {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public P2Pegasos(String prefix){
    super(prefix);
  }
  
  /**
   * Returns a new P2Pegasos object that initializes its variables with 
   * the deep copy of the specified parameter.
   * @param a learner to be cloned
   */
  protected P2Pegasos(P2Pegasos a){
    super(a);
  }
  
  public Object clone(){
    return new P2Pegasos(this);
  }

  /**
   * The official Pegasos update with the specified instances and corresponding label.
   */
  protected void gradient(SparseVector instance, double label) {
    gradient.set(w).mul(lambda);
    label = (label == 0.0) ? -1.0 : label;
    boolean isSV = label * w.mul(instance) < 1.0;
    if (isSV) {
      gradient.add(instance, -label);
    }
  }
  
  protected void gradient(InstanceHolder instances) {
    gradient.set(w).mul(lambda * instances.size());
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      label = (label == 0.0) ? -1.0 : label;
      boolean isSV = label * w.mul(instance) < 1.0;
      if (isSV) {
        gradient.add(instance, -label);
      }
    }
  }
  
  /**
   * Computes the inner product of the hyperplane and the specified instance. 
   * If it is greater than 0 then the label is positive (1.0), otherwise the label is
   * negative (0.0).</br></br>
   * The first value of the result vector is 0.0, the second is the value of 
   * the inner product.
   */
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double innerProd = w.mul(instance);
    distribution[0] = 0.0;
    distribution[1] = innerProd;
    return distribution;
  }

}
