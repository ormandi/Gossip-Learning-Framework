package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.MultiLogReg;

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
public class MergeableMultiLogReg extends MultiLogReg implements Mergeable, Partializable, Addable {
  private static final long serialVersionUID = -7800995106591726828L;

  /**
   * Default constructor that calls the super();
   */
  public MergeableMultiLogReg(String prefix) {
    super(prefix);
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
  
  @Override
  public Model merge(Model model) {
    MergeableMultiLogReg m = (MergeableMultiLogReg)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (int i = 0; i < numberOfClasses -1; i++) {
      w[i].mul(1.0 - modelWeight).add(m.w[i], modelWeight);
      bias[i] += (m.bias[i] - bias[i]) * modelWeight;
    }
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeableMultiLogReg m = (MergeableMultiLogReg)model;
    age += m.age * times;
    for (int i = 0; i < numberOfClasses -1; i++) {
      w[i].add(m.w[i], times);
      bias[i] += m.bias[i] * times;
    }
    return this;
  }

  @Override
  public Model getModelPart() {
    return new MergeableMultiLogReg(this);
  }

}
