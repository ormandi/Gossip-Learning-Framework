package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

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
public class OneVsAllMetaClassifier extends ProbabilityModel {
  private static final long serialVersionUID = 1650527797690827114L;
  /** @hidden */
  private static final String PAR_BNAME = "OvsA";
  
  protected int numberOfClasses;
  protected ModelHolder classifiers;
  /** @hidden */
  protected String baseLearnerName;
  /** @hidden */
  protected final String prefix;
  protected double[] distribution;

  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public OneVsAllMetaClassifier(String prefix) {
    this(prefix, PAR_BNAME);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_BNAME the prefix name in the configuration file
   */
  protected OneVsAllMetaClassifier(String prefix, String PAR_BNAME) {
    this.prefix = prefix + "." + PAR_BNAME;
    baseLearnerName = Configuration.getString(this.prefix + ".modelName");
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
      this.distribution = Arrays.copyOf(a.distribution, a.distribution.length);
    } else {
      classifiers = null;
      distribution = null;
    }
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param baseLearnerName name of the used learning algorithm
   * @param numberOfClasses number of classes
   * @param prefix
   * @param classifiers
   * @param distribution
   */
  protected OneVsAllMetaClassifier(String baseLearnerName, int numberOfClasses, String prefix, ModelHolder classifiers, double[] distribution) {
    this.baseLearnerName = baseLearnerName;
    this.numberOfClasses = numberOfClasses;
    this.prefix = prefix;
    this.classifiers = classifiers;
    this.distribution = distribution;
  }
  
  @Override
  public Object clone() {
    return new OneVsAllMetaClassifier(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    for (int i = 0; i < numberOfClasses; i++) {
      ((LearningModel)classifiers.getModel(i)).update(instance, (label == i) ? 1.0 : 0.0);
    }
  }
  
  public void update(InstanceHolder instances) {
    double[] labels = new double[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      labels[i] = instances.getLabel(i);
    }
    for (int i = 0; i < numberOfClasses; i++) {
      for (int j = 0; j < instances.size(); j++) {
        instances.setLabel(j, labels[j] == i ? 1.0 : 0.0);
      }
      ((LearningModel)classifiers.getModel(i)).update(instances);
    }
    for (int i = 0; i < instances.size(); i++) {
      instances.setLabel(i, labels[i]);
    }
    labels = null;
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
        ProbabilityModel model = (ProbabilityModel)Class.forName(baseLearnerName).getConstructor(String.class).newInstance(prefix);
        model.setNumberOfClasses(2);
        classifiers.add(model);
      } catch (Exception e) {
        throw new RuntimeException("Exception in class " + getClass().getCanonicalName(), e);
      }
    }
  }

  @Override
  public String toString() {
    return classifiers.toString();
  }

}
