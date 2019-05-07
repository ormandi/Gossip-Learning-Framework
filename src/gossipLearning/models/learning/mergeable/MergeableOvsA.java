package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.OneVsAllMetaClassifier;

public class MergeableOvsA extends OneVsAllMetaClassifier implements Mergeable, Partializable, Addable {
  private static final long serialVersionUID = -2294873002764150476L;
  
  public MergeableOvsA(String prefix) {
    super(prefix);
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

  @Override
  public Model merge(Model model) {
    MergeableOvsA m = (MergeableOvsA)model;
    age = Math.max(age, m.age);
    for (int i = 0; i < classifiers.size(); i++) {
      Model result = ((Mergeable)classifiers.getModel(i)).merge(m.classifiers.getModel(i));
      classifiers.setModel(i, result);
    }
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeableOvsA m = (MergeableOvsA)model;
    age += m.age * times;
    for (int i = 0; i < numberOfClasses; i++) {
      ((Addable)classifiers.getModel(i)).add(m.classifiers.getModel(i), times);
    }
    return this;
  }

  @Override
  public Model getModelPart() {
    return new MergeableOvsA(this);
  }
  
}
