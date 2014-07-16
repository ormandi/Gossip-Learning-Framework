package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.mergeable.MergeableOvsA;
import gossipLearning.utils.BQModelHolder;

import java.util.Arrays;
import java.util.Set;

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
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param baseLearnerName name of the used learning algorithm
   * @param numberOfClasses number of classes
   * @param prefix
   * @param classifiers
   * @param distribution
   */
  protected SlimOvsA(String baseLearnerName, int numberOfClasses, String prefix, ModelHolder classifiers, double[] distribution) {
    super(baseLearnerName, numberOfClasses, prefix, classifiers, distribution);
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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public SlimOvsA getModelPart(Set<Integer> indices) {
    ModelHolder classifiers = new BQModelHolder(this.classifiers.size());
    for (int i = 0; i < numberOfClasses; i++) {
      Model m = ((Partializable)this.classifiers.getModel(i)).getModelPart(indices);
      classifiers.add(m);
    }
    return new SlimOvsA(baseLearnerName, numberOfClasses, prefix, classifiers, Arrays.copyOf(distribution, distribution.length));
  }

}
