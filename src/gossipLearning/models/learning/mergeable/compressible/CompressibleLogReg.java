package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.interfaces.models.CompressibleModel;
import gossipLearning.models.learning.mergeable.MergeableLogReg;

/**
 * Compressible version of the logistic regression classifier.
 */
public class CompressibleLogReg extends MergeableLogReg implements CompressibleModel {
  
  public CompressibleLogReg(String prefix) {
    super(prefix);
  }
  
  /**
   * Returns a new CompressibleLogReg object that initializes its variables with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected CompressibleLogReg(CompressibleLogReg a) {
    super(a);
  }
  
  @Override
  public CompressibleLogReg clone() {
    return new CompressibleLogReg(this);
  }

}
