package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.multiclass.MultiLogReg;
import peersim.config.Configuration;

/**
 * This class represents a multi-class logistic regression classifier 
 * that can be merged to an other mergeable multi-class logistic regression classifier.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeableMultiLogReg.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeableMultiLogReg extends MultiLogReg implements Mergeable<MergeableMultiLogReg> {
  private static final long serialVersionUID = -7800995106591726828L;

  /** @hidden */
  protected static final String PAR_LAMBDA = "MergeableMultiLogReg.lambda";
  
  /**
   * Default constructor that calls the super();
   */
  public MergeableMultiLogReg() {
    super();
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  public MergeableMultiLogReg(MergeableMultiLogReg a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableMultiLogReg(this);
  }
  
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  @Override
  public MergeableMultiLogReg merge(MergeableMultiLogReg model) {
    age = Math.max(age, model.age);
    for (int i = 0; i < numberOfClasses; i++) {
      w[i].mul(0.5);
      w[i].add(model.w[i], 0.5);
      bias[i] = (bias[i] + model.bias[i]) / 2.0;
    }
    return this;
  }

}
