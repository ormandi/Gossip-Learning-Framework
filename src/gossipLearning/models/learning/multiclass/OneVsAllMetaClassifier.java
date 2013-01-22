package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This class represents a one-vs-all meta-classifier. It trains class size number of 
 * base learner. The base learners are trained as in two class classification tasks. The 
 * requirements for base learners is the followings: have to be extends from ProbabilityModel
 * and can handle the two class classification task.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>OVsA.modelName - name of the base learner</li>
 * <li>OVsA.modelName.param - base learner parameters</li>
 * </ul> 
 * @author István Hegedűs
 *
 */
public class OneVsAllMetaClassifier extends ProbabilityModel implements Mergeable<OneVsAllMetaClassifier> {
  private static final long serialVersionUID = 1650527797690827114L;
  /** @hidden */
  private static final String PAR_BNAME = "OVsA";
  
  private int numberOfClasses;
  private ModelHolder classifiers;
  /** @hidden */
  private String baseLearnerName;
  /** @hidden */
  private String prefix;
  private double[] distribution;

  /**
   * Default constructor (do nothing).
   */
  public OneVsAllMetaClassifier() {
  }
  
  /**
   * Copy constructor for deep copy
   * @param a to copy
   */
  public OneVsAllMetaClassifier(OneVsAllMetaClassifier a) {
    this.baseLearnerName = a.baseLearnerName;
    this.numberOfClasses = a.numberOfClasses;
    this.prefix = a.prefix;
    if (a.classifiers != null) {
      this.classifiers = (ModelHolder)a.classifiers.clone();
    } else {
      classifiers = null;
    }
  }
  
  @Override
  public Object clone() {
    return new OneVsAllMetaClassifier(this);
  }

  @Override
  public void init(String prefix) {
    this.prefix = prefix;
    baseLearnerName = Configuration.getString(prefix + "." + PAR_BNAME + ".modelName");
  }

  @Override
  public void update(SparseVector instance, double label) {
    for (int i = 0; i < numberOfClasses; i++) {
      ((LearningModel)classifiers.getModel(i)).update(instance, (label == i) ? 1.0 : 0.0);
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    for (int i = 0; i < numberOfClasses; i++) {
      double[] baseDistribution = ((ProbabilityModel)classifiers.getModel(i)).distributionForInstance(instance);
      distribution[i] = baseDistribution[1];
    }
    return distribution;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses < 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    this.numberOfClasses = numberOfClasses;
    distribution = new double[numberOfClasses];
    classifiers = new BQModelHolder(numberOfClasses);
    for (int i = 0; i < numberOfClasses; i++) {
      try {
        ProbabilityModel model = (ProbabilityModel)Class.forName(baseLearnerName).newInstance();
        model.init(prefix + "." + PAR_BNAME);
        model.setNumberOfClasses(2);
        classifiers.add(model);
      } catch (Exception e) {
        throw new RuntimeException("Exception in class " + getClass().getCanonicalName(), e);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public OneVsAllMetaClassifier merge(OneVsAllMetaClassifier model) {
    for (int i = 0; i < classifiers.size(); i++) {
      if (!(classifiers.getModel(i) instanceof Mergeable)) {
        return this;
      }
      Model result = ((Mergeable)classifiers.getModel(i)).merge(model.classifiers.getModel(i));
      classifiers.setModel(i, result);
    }
    return this;
  }

}