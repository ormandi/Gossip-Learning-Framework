package gossipLearning.models.adaptive;

import gossipLearning.interfaces.ErrorEstimatorModel;
import gossipLearning.interfaces.Model;
import gossipLearning.utils.LogNormalRandom;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class SelfAdaptiveModel implements ErrorEstimatorModel {
  private static final long serialVersionUID = 3943356691729519672L;
  private static final String PAR_MODELNAME = "model";
  private static final double mu = 8.0;
  //private static final double mu = 8.693147181;
  private static final double sigma = 0.5;
  //private static final double sigma = 0.25;
  
  private static final double C = 1.96;
  private static final double wsize = 100;
  private static final double alpha = 2.0/(wsize);
  private static LogNormalRandom r;
  protected String prefix;
  protected int numberOfClasses;
  
  protected Model model;
  protected double maximalAge;
  protected double age;
  protected String modelName;
  protected double meanError;
  protected double sqMeanError;
  protected double confidence;

  public SelfAdaptiveModel() {
    age = 2.0;
    meanError = 0.5;
    sqMeanError = 0.5;
    confidence = 0.0;
  }
  
  public SelfAdaptiveModel(SelfAdaptiveModel a) {
    if (a.model != null) {
      model = (Model)a.model.clone();
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
      model = (Model)Class.forName(modelName).newInstance();
      model.init(prefix);
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
        model = (Model)Class.forName(modelName).newInstance();
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
  
  public double getError() {
    return meanError + confidence;
  }
  
  public double getAge() {
    return age;
  }
  
  public String toString() {
    return age + "\t" + meanError + "\t" + confidence;
  }

}
