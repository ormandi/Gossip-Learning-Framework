package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.OneVsAllMetaClassifier;

public class MergeableOvsA extends OneVsAllMetaClassifier implements Mergeable<MergeableOvsA>, Partializable {
  private static final long serialVersionUID = -2294873002764150476L;
  
  /** @hidden */
  private static final String PAR_BNAME = "MergeableOvsA";
  
  public MergeableOvsA(String prefix) {
    super(prefix, PAR_BNAME);
  }
  
  protected MergeableOvsA(String prefix, String PAR_BNAME) {
    super(prefix, PAR_BNAME);
  }
  
  /**
   * Copy constructor for deep copy
   * @param a to copy
   */
  public MergeableOvsA(MergeableOvsA a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new MergeableOvsA(this);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public MergeableOvsA merge(MergeableOvsA model) {
    for (int i = 0; i < classifiers.size(); i++) {
      Model result = ((Mergeable)classifiers.getModel(i)).merge(model.classifiers.getModel(i));
      classifiers.setModel(i, result);
    }
    return this;
  }

  @Override
  public MergeableOvsA getModelPart() {
    return new MergeableOvsA(this);
  }

}
