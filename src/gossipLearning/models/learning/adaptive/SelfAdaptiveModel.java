package gossipLearning.models.learning.adaptive;

import gossipLearning.interfaces.models.ErrorEstimatorModel;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.LogNormalRandom;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

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
public class SelfAdaptiveModel implements ErrorEstimatorModel {
  private static final long serialVersionUID = 3943356691729519672L;
  private static final String PAR_MODELNAME = "SelfAdaptiveModel.model";
  private static final double mu = 8.0;
  private static final double sigma = 0.5;
  
  private static final double C = 1.96;
  private static final double wsize = 100;
  private static final double alpha = 2.0/(wsize);
  private static LogNormalRandom r;
  /** @hidden */
  protected String prefix;
  /**
   * The number of classes of the classification problem.
   */
  protected int numberOfClasses;
  
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
  public SelfAdaptiveModel() {
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
  public void init(String prefix) {
    this.prefix = prefix;
    long seed = Configuration.getLong("random.seed");
    if (r == null) {
      r = new LogNormalRandom(mu, sigma, seed);
    }
    maximalAge = r.nextDouble();
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    try {
      model = (LearningModel)Class.forName(modelName).newInstance();
      model.init(prefix + ".SelfAdaptiveModel");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void update(SparseVector instance, double label) {
    if (age >= maximalAge) {
      maximalAge = r.nextDouble();
      age = 2.0;
      meanError = 0.5;
      sqMeanError = 0.5;
      confidence = 0.0;
      try {
        model = (LearningModel)Class.forName(modelName).newInstance();
        model.init(prefix);
        model.setNumberOfClasses(numberOfClasses);
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
  public double predict(SparseVector instance) {
    return model.predict(instance);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
    model.setNumberOfClasses(numberOfClasses);
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
  
  public String toString() {
    return age + "\t" + meanError + "\t" + confidence;
  }

}
