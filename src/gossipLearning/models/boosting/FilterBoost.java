package gossipLearning.models.boosting;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.interfaces.WeakLearner;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.utils.Utils;

import java.util.Map;

import peersim.config.Configuration;

/**
 * This class represents a boosting algorithm namely the filter-boost.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>T - the number of boosting iterations</li>
 * <li>C - the base number of filtering instances</li>
 * <li>weakLearnerName - the name of the weak learner to use</li>
 * </ul>
 * @author István Hegedűs
 *
 */
public class FilterBoost implements Model, ProbabilityModel {
  private static final long serialVersionUID = 1654351368769565L;
  
  private static final String PAR_T = "T";
  private static final String PAR_C = "C";
  private static final String PAR_WEAKLEARNERNAME = "weakLearnerName";
  
  private String weakLearnerClassName;
  private WeakLearner actualWeakLearner;
  private ModelHolder strongLearner;
  
  private String prefix;
  private int numberOfClasses;
  
  private int T = 1;
  private int C = 1;
  
  private int t = 1;
  private int c = 1;
  private double actualEdge;
  private double sumWeights;
  private int ct;
  
  /**
   * Constructs an initial model.<br/>
   * NOTE: Object is not usable until calling init(String prefix) function!
   */
  public FilterBoost() {
    numberOfClasses = 2;
  }
  
  /**
   * Deep copy constructor.
   * @param a to copy
   */
  private FilterBoost(FilterBoost a) {
    this.T = a.T;
    this.C = a.C;
    this.t = a.t;
    this.c = a.c;
    this.ct = a.ct;
    this.prefix = a.prefix;
    this.numberOfClasses = a.numberOfClasses;
    this.weakLearnerClassName = Configuration.getString(prefix + "." + PAR_WEAKLEARNERNAME);
    if (a.actualWeakLearner != null) {
      this.actualWeakLearner = (WeakLearner)a.actualWeakLearner.clone();
    }
    this.actualEdge = a.actualEdge;
    this.sumWeights = a.sumWeights;
    this.strongLearner = (ModelHolder)a.strongLearner.clone();
  }
  
  /**
   * Deep copy.
   */
  public Object clone() {
    return new FilterBoost(this);
  }

  @Override
  public void init(String prefix) {
    this.prefix = prefix;
    T = Configuration.getInt(prefix + "." + PAR_T);
    C = Configuration.getInt(prefix + "." + PAR_C);
    weakLearnerClassName = Configuration.getString(prefix + "." + PAR_WEAKLEARNERNAME);
    strongLearner = new BoundedModelHolder(T);
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    if (t >= T) {
      // after iteration T do nothing
      return;
    }
    if (c == 1){
      // initializing a new weak learner
      ct = (int)(C * Math.log(t + 2));
      actualEdge = 0.0;
      sumWeights = 0.0;
      try {
        actualWeakLearner = (WeakLearner)Class.forName(weakLearnerClassName).newInstance();
        actualWeakLearner.init(prefix);
        actualWeakLearner.setNumberOfClasses(numberOfClasses);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Exception in FilterBoost at new week learner construction", e);
      }
    }
    if (c < ct) {
      // training weak learner
      double[] weights = getWeights(instance, label);
      actualWeakLearner.update(instance, label, weights);
    } else if (c < (2 * ct)) {
      // compute edge
      double[] weights = getWeights(instance, label);
      double alpha = computeAlpha(actualWeakLearner, instance, label, weights);
      actualWeakLearner.setAlpha(alpha);
    } else {
      // store weak learner
      storeWeekLearner(actualWeakLearner);
      c = 0;
      t ++;
    }
    c++;
  }
  
  /**
   * Returns the weight (alpha) of the specified weak learner respect to the parameters. 
   * The computation works iteratively.
   * @param weakLearner to compute for
   * @param instance to compute on
   * @param label label of instance
   * @param weights weights of labels
   * @return alpha
   */
  private double computeAlpha(WeakLearner weakLearner, Map<Integer, Double> instance, double label, double[] weights){
    double[] predictions = weakLearner.distributionForInstance(instance);
    for (int i = 0; i < numberOfClasses; i++) {
      double yl = (label == i) ? 1.0 : -1.0;
      double pl = (predictions[i] >= 0.0) ? 1.0 : -1.0;
      actualEdge += (pl == yl) ? weights[i] : -weights[i];
      sumWeights += weights[i];
    }
    double rate = (sumWeights == 0.0) ? 0.0 : actualEdge/sumWeights;
    if (rate >= 1.0) {
      rate = 1.0 - 0.0000001;
    }
    if (rate <= -1.0) {
      rate = -1.0 + 0.0000001;
    }
    return Math.log((1.0 + (rate)) / (1.0 - (rate))) / 2.0;
  }
  
  /**
   * Returns the vector of weights that correspond to the specified instance label pairs, 
   * based on prediction of the strong learner.
   * @param instance instance
   * @param label label index
   * @return weight vector
   */
  private double[] getWeights(Map<Integer, Double> instance, double label) {
    double[] weights = new double[numberOfClasses];
    for (int i = 0; i < weights.length; i++) {
      double cLabel = ((label == i) ? 1.0 : -1.0);
      weights[i] = 1.0 / (1.0 + Math.exp(predict(instance) * cLabel));
    }
    return weights;
  }
  
  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    double[] distribution = new double[numberOfClasses];
    // iterating through the week learners and aggregating the distributions
    for (int i = 0; i < strongLearner.size(); i++) {
      double[] tmpDist = ((WeakLearner)strongLearner.getModel(i)).distributionForInstance(instance);
      double alpha = ((WeakLearner)strongLearner.getModel(i)).getAlpha();
      for (int j = 0; j < distribution.length; j++){
        // updating the distribution
        distribution[j] += alpha * tmpDist[j];
      }
    }
    return Utils.normalize(distribution);
  }

  @Override
  public double predict(Map<Integer, Double> instance) {
    double maxValue = Double.NEGATIVE_INFINITY;
    double maxIndex = 0;
    double[] distribution = distributionForInstance(instance);
    // the prediction is the index of the class that has the maximal probability
    for (int i = 0; i < distribution.length; i++) {
      if (distribution[i] > maxValue) {
        maxValue = distribution[i];
        maxIndex = i;
      }
    }
    return maxIndex;
  }
  
  /**
   * Stores the specified model in a container.
   * @param model to store
   */
  private void storeWeekLearner(WeakLearner model){
    strongLearner.add((WeakLearner)model);
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }

}
