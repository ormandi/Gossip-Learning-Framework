package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.OneVsAllMetaClassifier;

import java.util.Set;

public class MergeableOvsA extends OneVsAllMetaClassifier implements Mergeable<MergeableOvsA>, Partializable<MergeableOvsA> {
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
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param baseLearnerName name of the used learning algorithm
   * @param numberOfClasses number of classes
   * @param prefix
   * @param classifiers
   * @param distribution
   */
  protected MergeableOvsA(String baseLearnerName, int numberOfClasses, String prefix, ModelHolder classifiers, double[] distribution) {
    super(baseLearnerName, numberOfClasses, prefix, classifiers, distribution);
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
  public MergeableOvsA getModelPart(Set<Integer> indices) {
    return new MergeableOvsA(this);
  }

}
