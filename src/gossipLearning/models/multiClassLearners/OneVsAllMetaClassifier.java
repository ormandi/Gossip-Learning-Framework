package gossipLearning.models.multiClassLearners;

import java.util.Map;

import peersim.config.Configuration;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.utils.Utils;

public class OneVsAllMetaClassifier extends ProbabilityModel {
  private static final long serialVersionUID = 1650527797690827114L;
  private static final String PAR_BNAME = "baseLearnerName";
  
  private int numberOfClasses;
  private ModelHolder classifiers;
  private String baseLearnerName;
  private String prefix;

  public OneVsAllMetaClassifier() {
  }
  
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
    baseLearnerName = Configuration.getString(prefix + "." + PAR_BNAME);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    for (int i = 0; i < numberOfClasses; i++) {
      classifiers.getModel(i).update(instance, (label == i) ? 1.0 : 0.0);
    }
  }

  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    double[] distribution = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      double[] baseDistribution = ((ProbabilityModel)classifiers.getModel(i)).distributionForInstance(instance);
      for (int j = 0; j < numberOfClasses; j++) {
        distribution[j] += baseDistribution[j];
      }
    }
    return Utils.normalize(distribution);
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
    classifiers = new BoundedModelHolder(numberOfClasses);
    for (int i = 0; i < numberOfClasses; i++) {
      try {
        ProbabilityModel model = (ProbabilityModel)Class.forName(baseLearnerName).newInstance();
        model.init(prefix);
        model.setNumberOfClasses(numberOfClasses);
        classifiers.add(model);
      } catch (Exception e) {
        throw new RuntimeException("Exception in class " + getClass().getCanonicalName(), e);
      }
    }
  }

}
