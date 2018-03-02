package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.mergeable.MergeableOvsA;

public class SlimOvsA extends MergeableOvsA {
  private static final long serialVersionUID = 4459146413742898799L;
  
  /** @hidden */
  private static final String PAR_BNAME = "SlimOvsA";
  
  /**
   * Default constructor (do nothing).
   */
  public SlimOvsA(String prefix) {
    super(prefix, PAR_BNAME);
  }
  
  /**
   * Copy constructor for deep copy
   * @param a to copy
   */
  public SlimOvsA(SlimOvsA a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new SlimOvsA(this);
  }

  @Override
  public SlimOvsA merge(MergeableOvsA model) {
    super.merge(model);
    return this;
  }
  
  /*@Override
  public void update(InstanceHolder instances) {
    int idx = CommonState.r.nextInt(instances.size());
    SparseVector instance = instances.getInstance(idx);
    double label = instances.getLabel(idx);
    super.update(instance, label);
  }*/

  @Override
  public SlimOvsA getModelPart() {
    SlimOvsA result = new SlimOvsA(this);
    result.classifiers.clear();
    for (int i = 0; i < numberOfClasses; i++) {
      Model m = ((Partializable)this.classifiers.getModel(i)).getModelPart();
      result.classifiers.add(m);
    }
    return result;
  }

}
