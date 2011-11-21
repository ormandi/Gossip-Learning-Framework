package gossipLearning.models.boosting;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.interfaces.WeakLearner;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.weakLearners.ConstantLearner;
import gossipLearning.utils.MapComparator;

import java.util.Map;
import java.util.TreeMap;

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
public class FilterBoost extends ProbabilityModel {
  private static final long serialVersionUID = 1654351368769565L;
  
  private static final String PAR_T = "T";
  private static final String PAR_C = "C";
  private static final String PAR_WEAKLEARNERNAME = "weakLearnerName";
  
  private String weakLearnerClassName;
  private WeakLearner actualWeakLearner;
  private WeakLearner constantWeakLearner;
  private ModelHolder strongLearner;
  
  private String prefix;
  private int numberOfClasses;
  
  private int T = 1;
  private int C = 1;
  
  private int t = 1;
  private int c = 1;
  private double weakEdge;
  private double weakWeights;
  private double constantEdge;
  private double constantWeights;
  private int ct;
  
  private static Map<Map<Integer, Double>, double[]> cacheDist = new TreeMap<Map<Integer,Double>, double[]>(new MapComparator<Map<Integer,Double>>());
  
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
    if (a.constantWeakLearner != null) {
      this.constantWeakLearner = (WeakLearner)a.constantWeakLearner.clone();
    }
    this.weakEdge = a.weakEdge;
    this.weakWeights = a.weakWeights;
    this.constantEdge = a.constantEdge;
    this.constantWeights = a.constantWeights;
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
      weakEdge = 0.0;
      constantEdge = 0.0;
      weakWeights = 0.0;
      constantWeights = 0.0;
      try {
        actualWeakLearner = (WeakLearner)Class.forName(weakLearnerClassName).newInstance();
        actualWeakLearner.init(prefix);
        actualWeakLearner.setNumberOfClasses(numberOfClasses);
        constantWeakLearner = new ConstantLearner();
        constantWeakLearner.init(prefix);
        constantWeakLearner.setNumberOfClasses(numberOfClasses);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Exception in FilterBoost at new week learner construction", e);
      }
    }
    if (c < ct) {
      // training weak learner
      double[] weights = getWeights(instance, label);
      actualWeakLearner.update(instance, label, weights);
      constantWeakLearner.update(instance, label, weights);
    } else if (c < (2 * ct)) {
      // compute edge
      double[] weights = getWeights(instance, label);
      double[] computed = computeAlpha(actualWeakLearner, weakEdge, weakWeights, instance, label, weights);
      actualWeakLearner.setAlpha(computed[0]);
      weakEdge = computed[1];
      weakWeights = computed[2];
      computed = computeAlpha(constantWeakLearner, constantEdge, constantWeights, instance, label, weights);
      constantWeakLearner.setAlpha(computed[0]);
      constantEdge = computed[1];
      constantWeights = computed[2];
    } else {
      // store weak learner
      if (actualWeakLearner.getAlpha() > constantWeakLearner.getAlpha()) {
        storeWeekLearner(actualWeakLearner);
      } else {
        storeWeekLearner(constantWeakLearner);
      }
      c = 0;
      t ++;
    }
    c++;
  }
  
  /**
   * Returns the array of [alpha, edge, sumWeigth] of the specified weak learner respect to the parameters. 
   * The computation works iteratively.
   * @param weakLearner to compute for
   * @param instance to compute on
   * @param label label of instance
   * @param weights weights of labels
   * @return array of [alpha, edge, sumWeigth]
   */
  private static double[] computeAlpha(WeakLearner weakLearner, double weakEdge, double weakWeigth, Map<Integer, Double> instance, double label, double[] weights){
    double[] predictions = weakLearner.distributionForInstance(instance);
    for (int i = 0; i < weights.length; i++) {
      double yl = (label == i) ? 1.0 : -1.0;
      double pl = (predictions[i] >= 0.0) ? 1.0 : -1.0;
      weakEdge += (pl == yl) ? weights[i] : -weights[i];
      weakWeigth += weights[i];
    }
    double rate = (weakWeigth == 0.0) ? 0.0 : weakEdge/weakWeigth;
    if (rate >= 1.0) {
      rate = 1.0 - 0.0000001;
    }
    if (rate <= -1.0) {
      rate = -1.0 + 0.0000001;
    }
    return new double[]{Math.log((1.0 + (rate)) / (1.0 - (rate))) / 2.0, weakEdge, weakWeigth};
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
    double[] distribution = distributionForInstance(instance);
    for (int i = 0; i < weights.length; i++) {
      double cLabel = ((label == i) ? 1.0 : -1.0);
      weights[i] = 1.0 / (1.0 + Math.exp(distribution[i] * cLabel));
    }
    return weights;
  }
  
  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    if (cacheDist.containsKey(instance)) {
      return cacheDist.get(instance);
    }
    double[] distribution = new double[numberOfClasses];
    // iterating through the week learners and aggregating the distributions
    for (int i = 0; i < strongLearner.size(); i++) {
      double[] tmpDist = ((WeakLearner)strongLearner.getModel(i)).distributionForInstance(instance);
      double alpha = ((WeakLearner)strongLearner.getModel(i)).getAlpha();
      for (int j = 0; j < distribution.length; j++){
        // updating the distribution
        //distribution[j] += alpha * tmpDist[j];
        distribution[j] += alpha * (tmpDist[j] < 0.0 ? -1.0 : 1.0);
      }
    }
    cacheDist.put(instance, distribution);
    return distribution;
  }
  
  /**
   * Stores the specified model in a container.
   * @param model to store
   */
  private void storeWeekLearner(WeakLearner model){
    strongLearner.add((WeakLearner)model);
    double[] distribution;
    double[] cachedDistribution;
    for (Map<Integer, Double> instance : cacheDist.keySet()) {
      distribution = model.distributionForInstance(instance);
      cachedDistribution = cacheDist.get(instance);
      for (int i = 0; i < distribution.length; i++) {
        cachedDistribution[i] += model.getAlpha() * (distribution[i] < 0.0 ? -1.0 : 1.0);
      }
    }
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < strongLearner.size(); i++) {
      if (i > 0) {
        sb.append("\t" + strongLearner.getModel(i));
      } else {
        sb.append(strongLearner.getModel(i));
      }
    }
    return sb.toString();
  }
  
  public double getAlpha(int index) {
    return ((WeakLearner)strongLearner.getModel(index)).getAlpha();
  }
  
  public int getSmallC() {
    return c;
  }
  
  public int getSmallT() {
    return t;
  }

}
