package gossipLearning.models.learning.adaptive;

import gossipLearning.interfaces.models.ErrorEstimatorModel;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.BoundedQueue;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * A type of model, that can handle the drifting concepts using a drift detection 
 * method that based on the gradient of the historical performance.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>SelfAdaptiveModelTH.historyLength - the length of the prediction history</li>
 * <li>SelfAdaptiveModelTH.model - the name of the used base-model</li>
 * <li>The parameters of the specified base-model</li>
 * </ul>
 * @author István Hegedűs
 */
public class SelfAdaptiveModelTH implements ErrorEstimatorModel {
  private static final long serialVersionUID = 3943356691729519672L;
  
  private static final String PAR_MODELNAME = "SelfAdaptiveModelTH.model";
  private String modelName = null;
  private String prefix = null;
  
  private static final String PAR_HLENGTH = "SelfAdaptiveModelTH.historyLength";
  private int historyLength;
  
  private LearningModel model = null;
  private int numOfClasses;
  private BoundedQueue<Double> history;
  
  public SelfAdaptiveModelTH() {
    age = 0;
  }
  
  /**
   * Constructs an object that is a deep copy of the specified object.
   * @param a to be clone.
   */
  @SuppressWarnings("unchecked")
  protected SelfAdaptiveModelTH(SelfAdaptiveModelTH a) {
    this();
    age = a.age;
    isNewModel = a.isNewModel;
    numOfClasses = a.numOfClasses;
    historyLength = a.historyLength;
    prefix = a.prefix;
    history = new BoundedQueue<Double>(historyLength);
    if (a.modelName != null) {
      modelName = new String(a.modelName);
    }
    if (a.model != null) {
      model = (LearningModel)a.model.clone();
    }
    if (a.history != null) {
      history = (BoundedQueue<Double>)a.history.clone();
    }
    if (a.errors != null) {
      errors = a.errors.clone();
    }
    if (a.ab != null) {
      ab = a.ab.clone();
    }
    error = a.error;
  }
  
  @Override
  public Object clone() {
    return new SelfAdaptiveModelTH(this);
  }
  
  @Override
  public void init(String prefix) {
    this.prefix = new String(prefix);
    historyLength = Configuration.getInt(prefix + "." + PAR_HLENGTH);
    if (historyLength % 2 != 0) {
      historyLength++;
    }
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    if (model == null) {
      try {
        model = (LearningModel)Class.forName(modelName).newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    model.init(prefix + ".SelfAdaptiveModelTH");
    history = new BoundedQueue<Double>(historyLength);
  }
  
  private double age = 0;
  private boolean isNewModel = true;
  @Override
  public void update(SparseVector instance, double label) {
    isNewModel = false;
    age ++;
    double pred = model.predict(instance);
    if (pred == label) {
      history.add(0.0);
    } else {
      history.add(1.0);
    }
    if (isCreateNewModel()) {
      //System.err.println("#NEWMODEL:" + this);
      try {
        model = (LearningModel)Class.forName(modelName).newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      model.init(prefix + ".SelfAdaptiveModelTH");
      model.setNumberOfClasses(numOfClasses);
      age = 0;
      isNewModel = true;
    }
    model.update(instance, label);
  }
  
  private double error;
  private double[] errors;
  private double[] ab;
  /**
   * This function detects that new model creation is crucial to keep the 
   * convergence and performance of the system
   * @return true if new model creation is crucial, false otherwise
   */
  protected boolean isCreateNewModel() {
    if (age < historyLength) {
      return false;
    }
    // features: time based performance, derivatives, autocorrelations
    int len = historyLength / 2;
    error = 0.0;
    if (errors == null) {
      errors = new double[len];
    }
    double act = 0.0;
    for (int i = 0; i < len; i++) {
      act = 0.0;
      for (int j = i; j < i + len; j++) {
        act += history.get(j);// == -1.0 ? 1.0 : 0.0;
      }
      act /= len;
      errors[i] = act;
      error += act;
    }
    for (int i = 0; i < history.size(); i++) {
      error += history.get(i);
    }
    error /= history.size();
    ab = Utils.regression(errors);
    if (ab[0] < 0.0) {
      return false;
    }
    double c = 20;
    double d = 0.5;
    double val = 1.0/(1.0 + Math.exp(-c*(ab[0] - d)));
    return CommonState.r.nextDouble() < val;
  }

  @Override
  public double predict(SparseVector instance) {
    return model.predict(instance);
  }

  @Override
  public int getNumberOfClasses() {
    return numOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    model.setNumberOfClasses(numberOfClasses);
    this.numOfClasses = numberOfClasses;
  }
  
  /**
   * Returns true if the model is restarted.
   * @return the model is restarted.
   */
  public boolean isNewModel() {
    return isNewModel;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(((ab == null) ? "null" : ab[0] + "," + ab[1] + ""));
    if (errors == null) {
      sb.append(",null");
    } else {
      for (int i = 0; i < errors.length; i++) {
        if (i == 0) {
          sb.append(',');
        } else {
          sb.append(' ');
        }
        sb.append(errors[i]);
      }
    }
    return sb.toString();
  }

  @Override
  public double getError() {
    return error;
  }
  
  @Override
  public double getAge() {
    return age;
  }

}
