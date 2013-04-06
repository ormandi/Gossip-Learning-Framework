package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.OneVsAllMetaClassifier;

import java.util.Arrays;
import java.util.Set;

import peersim.config.Configuration;

public class MergeableOVsA extends OneVsAllMetaClassifier implements Mergeable<MergeableOVsA>, Partializable<MergeableOVsA> {
  private static final long serialVersionUID = -2294873002764150476L;
  
  /** @hidden */
  private static final String PAR_BNAME = "MergeableOVsA";
  
  /**
   * Default constructor (do nothing).
   */
  public MergeableOVsA() {
    super();
  }
  
  /**
   * Copy constructor for deep copy
   * @param a to copy
   */
  public MergeableOVsA(MergeableOVsA a) {
    this.baseLearnerName = a.baseLearnerName;
    this.numberOfClasses = a.numberOfClasses;
    this.prefix = a.prefix;
    if (a.classifiers != null) {
      this.classifiers = (ModelHolder)a.classifiers.clone();
    } else {
      classifiers = null;
    }
    this.distribution = Arrays.copyOf(a.distribution, a.distribution.length);
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param baseLearnerName name of the used learning algorithm
   * @param numberOfClasses number of classes
   * @param prefix
   * @param classifiers
   * @param distribution
   */
  protected MergeableOVsA(String baseLearnerName, int numberOfClasses, 
      String prefix, ModelHolder classifiers, double[] distribution) {
    this.baseLearnerName = baseLearnerName;
    this.numberOfClasses = numberOfClasses;
    this.prefix = prefix;
    this.classifiers = classifiers;
    this.distribution = distribution;
  }
  
  @Override
  public Object clone() {
    return new MergeableOVsA(this);
  }

  @Override
  public void init(String prefix) {
    this.prefix = prefix;
    baseLearnerName = Configuration.getString(prefix + "." + PAR_BNAME + ".modelName");
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public MergeableOVsA merge(MergeableOVsA model) {
    for (int i = 0; i < classifiers.size(); i++) {
      Model result = ((Mergeable)classifiers.getModel(i)).merge(model.classifiers.getModel(i));
      classifiers.setModel(i, result);
    }
    return this;
  }

  @Override
  public MergeableOVsA getModelPart(Set<Integer> indices) {
    return this;
  }

}
