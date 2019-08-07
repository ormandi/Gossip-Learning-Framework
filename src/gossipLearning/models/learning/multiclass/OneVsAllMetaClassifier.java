package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.InstanceHolder;
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
public class OneVsAllMetaClassifier extends ProbabilityModel {
  private static final long serialVersionUID = 1650527797690827114L;
  protected ModelHolder classifiers;
  /** @hidden */
  protected String baseLearnerName;
  /** @hidden */
  protected final String prefix;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public OneVsAllMetaClassifier(String prefix) {
    super(prefix);
    this.prefix = prefix;
    baseLearnerName = Configuration.getString(prefix + ".model");
  }
  
  /**
   * Copy constructor for deep copy
   * @param a to copy
   */
  public OneVsAllMetaClassifier(OneVsAllMetaClassifier a) {
    super(a);
    this.baseLearnerName = a.baseLearnerName;
    this.prefix = a.prefix;
    if (a.classifiers != null) {
      this.classifiers = a.classifiers.clone();
    } else {
      classifiers = null;
    }
  }
  
  @Override
  public OneVsAllMetaClassifier clone() {
    return new OneVsAllMetaClassifier(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    for (int i = 0; i < numberOfClasses; i++) {
      ((LearningModel)classifiers.getModel(i)).update(instance, (label == i) ? 1.0 : 0.0);
    }
  }
  
  public final void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
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
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    classifiers = new BQModelHolder(numberOfClasses);
    for (int i = 0; i < numberOfClasses; i++) {
      try {
        ProbabilityModel model = (ProbabilityModel)Class.forName(baseLearnerName).getConstructor(String.class).newInstance(prefix);
        model.setParameters(2, numberOfFeatures);
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
  
  @Override
  public void clear() {
    super.clear();
    for (int i = 0; i < numberOfClasses; i++) {
      ProbabilityModel model = (ProbabilityModel)classifiers.getModel(i);
      model.clear();
    }
  }
  
  @Override
  public void setAge(double age) {
    super.setAge(age);
    for (int i = 0; i < numberOfClasses; i++) {
      ProbabilityModel model = (ProbabilityModel)classifiers.getModel(i);
      model.setAge(age);
    }
  }
  
  @Override
  public Model set(Model model) {
    OneVsAllMetaClassifier m = (OneVsAllMetaClassifier)model;
    for (int i = 0; i < numberOfClasses; i++) {
      classifiers.getModel(i).set(m.classifiers.getModel(i));
    }
    return this;
  }

}
