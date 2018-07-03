package gossipLearning.models.learning.adaptive;

import gossipLearning.interfaces.models.ErrorEstimatorModel;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * A type of model, that can handle the drifting concepts through 
 * keeps divers the age of the models in the network based on 
 * the predefined life time of the models using renewal process theorems.
 * <br/><br/>
 * The life time comes from a log-normal distribution with parameters: 
 * mu = 0.8, sigma = 0.5.
 * <br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>SelfAdaptiveModel.model - the name of the used base-model</li>
 * <li>The parameters of the specified base-model</li>
 * </ul>
 * @author István Hegedűs
 */
public class SelfAdaptiveModel  implements ErrorEstimatorModel {
  private static final long serialVersionUID = 3943356691729519672L;
  private static final String PAR_MODELNAME = "SelfAdaptiveModel.model";
  private static final double mu = 8.0;
  private static final double sigma = 0.5;
  
  private static final double C = 1.96;
  private static final double wsize = 100;
  private static final double alpha = 2.0/(wsize);
  /** @hidden */
  protected String prefix;
  /**
   * The number of classes of the classification problem.
   */
  protected int numberOfClasses;
  protected int numberOfFeatures;
  
  /**
   * The classification model.
   */
  protected LearningModel model;
  /**
   * The maximal living age of the model.
   */
  protected double maximalAge;
  /**
   * The current age of the model.
   */
  protected double age;
  /**
   * The canonical name of the model.
   * @hidden
   */
  protected String modelName;
  /**
   * The estimated mean error of the model.
   */
  protected double meanError;
  /**
   * The estimated squared mean error of the model.
   */
  protected double sqMeanError;
  /**
   * The expected confidence of the estimated error.
   */
  protected double confidence;

  /**
   * Constructs an initial object, call of init(prefix) function is required.
   */
  public SelfAdaptiveModel(String prefix) {
    this.prefix = prefix;
    maximalAge = Utils.nextLogNormal(mu, sigma, CommonState.r);
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    try {
      model = (LearningModel)Class.forName(modelName).getConstructor(String.class).newInstance(prefix + ".SelfAdaptiveModel");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    age = 2.0;
    meanError = 0.5;
    sqMeanError = 0.5;
    confidence = 0.0;
  }
  
  /**
   * Constructs an object that is a deep copy of the specified object.
   * @param a to be clone.
   */
  public SelfAdaptiveModel(SelfAdaptiveModel a) {
    if (a.model != null) {
      model = (LearningModel)a.model.clone();
    }
    if (a.modelName != null) {
      modelName = new String(a.modelName);
    }
    prefix = a.prefix;
    numberOfClasses = a.numberOfClasses;
    numberOfFeatures = a.numberOfFeatures;
    age = a.age;
    maximalAge = a.maximalAge;
    meanError = a.meanError;
    sqMeanError = a.sqMeanError;
    confidence = a.confidence;
  }
  
  public Object clone() {
    return new SelfAdaptiveModel(this);
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    if (age >= maximalAge) {
      maximalAge = Utils.nextLogNormal(mu, sigma, CommonState.r);
      age = 2.0;
      meanError = 0.5;
      sqMeanError = 0.5;
      confidence = 0.0;
      try {
        model = (LearningModel)Class.forName(modelName).getConstructor(String.class).newInstance(prefix);
        model.setParameters(numberOfClasses, numberOfFeatures);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      //System.out.println("#NEW MODEL " + this);
    }
    age++;
    double prediction = model.predict(instance);
    double error = label == prediction ? 0.0 : 1.0;
    model.update(instance, label);
    //meanError = meanError*(1.0 - 1.0/age) + error/age;
    meanError = (1.0 - alpha)*meanError + alpha*error;
    sqMeanError = (1.0 - alpha)*sqMeanError + alpha*(error*error);
    double std = Math.sqrt(sqMeanError - (meanError * meanError));
    confidence = C*std/Math.sqrt(Math.min(age, wsize));
  }
  
  @Override
  public void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }
  
  @Override
  public void update(InstanceHolder instances, int epoch, int batchSize) {
    for (int i = 0; i < epoch; i++) {
      update(instances);
    }
  }

  @Override
  public double predict(SparseVector instance) {
    return model.predict(instance);
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    this.numberOfClasses = numberOfClasses;
    this.numberOfFeatures = numberOfFeatures;
    model.setParameters(numberOfClasses, numberOfFeatures);
  }
  
  @Override
  public void clear() {
    maximalAge = Utils.nextLogNormal(mu, sigma, CommonState.r);
    model.clear();
    age = 2.0;
    meanError = 0.5;
    sqMeanError = 0.5;
    confidence = 0.0;
  }

  /**
   * Returns the estimated error plus the confidence.
   */
  public double getError() {
    return meanError + confidence;
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public void setAge(double age) {
    this.age = age;
  }
  
  public String toString() {
    return age + "\t" + meanError + "\t" + confidence;
  }

}
