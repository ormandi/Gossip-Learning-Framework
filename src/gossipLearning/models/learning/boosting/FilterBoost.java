package gossipLearning.models.learning.boosting;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.models.learning.boosting.weakLearners.ConstantLearner;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;

/**
 * This class represents a boosting algorithm namely the filter-boost.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>FilterBoost.T - the number of boosting iterations</li>
 * <li>FilterBoost.C - the base number of filtered instances per iterations</li>
 * <li>FilterBoost.useCache - using cache for distributions or not</li>
 * <li>FilterBoost.weakLearnerName - the name of the weak learner to use</li>
 * </ul>
 * @author István Hegedűs
 *
 */
public class FilterBoost extends ProbabilityModel {
  private static final long serialVersionUID = 1654351368769565L;
  
  private static final String PAR_T = "FilterBoost.T";
  private static final String PAR_C = "FilterBoost.C";
  private static final String PAR_USECACHE = "FilterBoost.useCache";
  private static final String PAR_WEAKLEARNERNAME = "FilterBoost.weakLearnerName";
  
  private String weakLearnerClassName;
  private WeakLearner actualWeakLearner;
  private WeakLearner constantWeakLearner;
  protected ModelHolder strongLearner;
  
  private String prefix;
  private int numberOfClasses;
  
  protected int T = 1;
  private int C = 1;
  private boolean useCache;
  
  /**
   * The number of stored weak learners.
   */
  protected int t = 0;
  private int c = 1;
  private double weakEdge;
  private double weakWeights;
  private double constantEdge;
  private double constantWeights;
  private int ct;
  
  /** @hidden */
  private Map<SparseVector, double[]> cacheDist;
  
  /**
   * Constructs an initial model.<br/>
   * @NOTE: Object is not usable until calling init(String prefix) function!
   */
  public FilterBoost() {
    numberOfClasses = 2;
    cacheDist = new TreeMap<SparseVector, double[]>();
    useCache = true;
  }
  
  /**
   * Deep copy constructor.
   * @param a to copy
   */
  protected FilterBoost(FilterBoost a) {
    this();
    this.T = a.T;
    this.C = a.C;
    this.t = a.t;
    this.c = a.c;
    this.ct = a.ct;
    this.prefix = a.prefix;
    this.useCache = a.useCache;
    this.numberOfClasses = a.numberOfClasses;
    this.weakLearnerClassName = a.weakLearnerClassName;
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
    if (a.losses != null) {
      this.losses = a.losses.clone();
    }
    if (a.sWeigths != null) {
      this.sWeigths = a.sWeigths.clone();
    }
    this.counter = a.counter;
    this.cumulativeError = a.cumulativeError;
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
    useCache = Configuration.getBoolean(prefix + "." + PAR_USECACHE);
    weakLearnerClassName = Configuration.getString(prefix + "." + PAR_WEAKLEARNERNAME);
    strongLearner = new BQModelHolder(T);
  }

  private double[] losses;
  private double[] sWeigths;
  private double counter = 0.0;
  private double cumulativeError = 1.0;
  
  @Override
  public void update(SparseVector instance, double label) {
    if (c == 1){
      // compute the cumulative error and fill strong learner weights with 0
      if (counter == 0) {
        cumulativeError = 1.0;
      } else {
        cumulativeError = 0.0;
        for (int i = 0; i < sWeigths.length; i++) {
          cumulativeError += sWeigths[i];
          sWeigths[i] = 0.0;
        }
        cumulativeError /= sWeigths.length;
      }
      counter = 0.0;
      
      // initializing a new weak learner
      ct = (int)(C * Math.log(t + 2));
      weakEdge = 0.0;
      constantEdge = 0.0;
      weakWeights = 0.0;
      constantWeights = 0.0;
      try {
        actualWeakLearner = (WeakLearner)Class.forName(weakLearnerClassName).newInstance();
        actualWeakLearner.init(prefix + ".FilterBoost");
        actualWeakLearner.setNumberOfClasses(numberOfClasses);
        constantWeakLearner = new ConstantLearner();
        constantWeakLearner.init(prefix + ".FilterBoost");
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
      double[] distribution = distributionForInstance(instance);
      double[] weights = getWeights(distribution, label);
      counter++;
      for (int i = 0; i < weights.length; i++) {
        sWeigths[i] = (1.0 - 1.0/counter) * sWeigths[i] + (1.0/counter) * weights[i];
      }
      
      // compute edge
      double[] weakDist = constantWeakLearner.distributionForInstance(instance);
      double[] computed = computeAlpha(weakDist, constantEdge, constantWeights, instance, label, weights);
      constantWeakLearner.setAlpha(computed[0]);
      constantEdge = computed[1];
      constantWeights = computed[2];
      weakDist = actualWeakLearner.distributionForInstance(instance);
      computed = computeAlpha(weakDist, weakEdge, weakWeights, instance, label, weights);
      actualWeakLearner.setAlpha(computed[0]);
      weakEdge = computed[1];
      weakWeights = computed[2];
      
      // update the losses for the weak learners
      if (strongLearner.size() >= T) {
        if (losses == null) {
          losses = new double[T];
        }
        for (int i = 0; i < T; i++) {
          double[] dist = ((WeakLearner)strongLearner.getModel(i)).distributionForInstance(instance);
          double alpha = ((WeakLearner)strongLearner.getModel(i)).getAlpha();
          for(int l = 0; l < distribution.length; l++) {
            double cLabel = label == l ? 1.0 : -1.0;
            double pLabel = dist[l] < 0.0 ? -1.0 : 1.0;
            double distDiff = distribution[l] - (alpha * pLabel);
            losses[i] += (1.0 / (1.0 + Math.exp(distDiff * cLabel))) * Math.exp(-cLabel * (weakDist[l] < 0.0 ? -1.0 : 1.0));
          }
        }
      }
    } else {
      
      // remove the worst weak learner and fill the losses with 0.0
      if (strongLearner.size() >= T) {
        int idx = losses.length -1;
        double min = losses[idx];
        losses[idx] = 0.0;
        for (int i = losses.length -2; i >= 0; i--) {
          if (losses[i] < min) {
            min = losses[i];
            idx = i;
          }
          losses[i] = 0.0;
        }
        removeWeekLearner(idx);
      }
      
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
   * @param class distribution of weakLearner to compute for
   * @param instance to compute on
   * @param label label of instance
   * @param weights weights of labels
   * @return array of [alpha, edge, sumWeigth]
   */
  private static double[] computeAlpha(double[] distribution, double weakEdge, double weakWeigth, SparseVector instance, double label, double[] weights){
    for (int i = 0; i < weights.length; i++) {
      double yl = (label == i) ? 1.0 : -1.0;
      double pl = (distribution[i] < 0.0) ? -1.0 : 1.0;
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
   * Returns the vector of weights that correspond to the class distribution of the 
   * specified instance label pairs, based on prediction of the strong learner.
   * @param distribution distribution
   * @param label label index
   * @return weight vector
   */
  private double[] getWeights(double[] distribution, double label) {
    double[] weights = new double[distribution.length];
    for (int i = 0; i < weights.length; i++) {
      double cLabel = label == i ? 1.0 : -1.0;
      weights[i] = 1.0 / (1.0 + Math.exp(distribution[i] * cLabel));
    }
    return weights;
  }
  
  /**
   * Returns the vector of weights that correspond to the specified instance label pairs, 
   * based on prediction of the strong learner.
   * @param instance instance
   * @param label label index
   * @return weight vector
   */
  private double[] getWeights(SparseVector instance, double label) {
    double[] distribution = distributionForInstance(instance);
    return getWeights(distribution, label);
  }
  
  /**
   * Returns the class distribution for the specified instance. Gets the distribution from 
   * cache if it is already computed or calls the distributionForInstance function to compute 
   * it and stores them in the cache.
   * @param instance compute the distribution for
   * @return computed distribution
   */
  private double[] cacheDistributionForInstance(SparseVector instance) {
    double[] distribution = cacheDist.get(instance);
    if (distribution != null) {
      return distribution;
    }
    distribution = computeDistributionForInstance(instance);
    cacheDist.put(instance, distribution.clone());
    return distribution;
  }
  
  /**
   * Computes the class distribution for the specified instance based on the FilterBoost 
   * class distribution rule.
   * @param instance compute distribution for
   * @return computed class distribution
   */
  private double[] distribution_qqq;
  private double[] computeDistributionForInstance(SparseVector instance) {
    if (distribution_qqq == null) {
      distribution_qqq = new double[numberOfClasses];
    }
    Arrays.fill(distribution_qqq, 0.0);
    // iterating through the week learners and aggregating the distributions
    for (int i = 0; i < strongLearner.size(); i++) {
      double[] tmpDist = ((WeakLearner)strongLearner.getModel(i)).distributionForInstance(instance);
      double alpha = ((WeakLearner)strongLearner.getModel(i)).getAlpha();
      for (int j = 0; j < distribution_qqq.length; j++){
        // updating the distribution
        distribution_qqq[j] += alpha * (tmpDist[j] < 0.0 ? -1.0 : 1.0);
      }
    }
    return distribution_qqq;
  }
  
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    if (useCache) {
      // using cached and updated class distributions
      return cacheDistributionForInstance(instance);
    } else {
      // in case of fully online manner
      return computeDistributionForInstance(instance);
    }
  }
  
  /**
   * Stores the specified model in a container.
   * @param model to store
   */
  protected void storeWeekLearner(WeakLearner model) {
    strongLearner.add((WeakLearner)model);
    double[] distribution;
    double[] cachedDistribution;
    for (SparseVector instance : cacheDist.keySet()) {
      distribution = model.distributionForInstance(instance);
      cachedDistribution = cacheDist.get(instance);
      for (int i = 0; i < distribution.length; i++) {
        cachedDistribution[i] += model.getAlpha() * (distribution[i] < 0.0 ? -1.0 : 1.0);
      }
    }
  }
  
  /**
   * Removes the weak learner from the strong learner at the specified index.
   * @param index index of model to remove
   */
  protected void removeWeekLearner(int index) {
    WeakLearner model = (WeakLearner)strongLearner.remove(index);
    double[] distribution;
    double[] cachedDistribution;
    for (SparseVector instance : cacheDist.keySet()) {
      distribution = model.distributionForInstance(instance);
      cachedDistribution = cacheDist.get(instance);
      for (int i = 0; i < distribution.length; i++) {
        cachedDistribution[i] -= model.getAlpha() * (distribution[i] < 0.0 ? -1.0 : 1.0);
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
    if (sWeigths == null) {
      sWeigths = new double[numberOfClasses];
    }
  }
  
  /**
   * String representation of the current object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("c=" + c + "\tct=" + ct + "\tt=" + t + "\tT=" + T + "\t");
    for (int i = 0; i < strongLearner.size(); i++) {
      if (i > 0) {
        sb.append("\t" + strongLearner.getModel(i));
      } else {
        sb.append(strongLearner.getModel(i));
      }
    }
    sb.append("\tact:" + actualWeakLearner);
    return sb.toString();
  }
  
  /**
   * Returns the coefficient of the weak learner at the specified index.
   * @param index index of model to get coefficient
   * @return coefficient (alpha)
   */
  public double getAlpha(int index) {
    return ((WeakLearner)strongLearner.getModel(index)).getAlpha();
  }
  
  /**
   * Returns the value that represents the state of the learning phase of the
   * algorithm
   * @return learning phase indicator
   */
  public int getSmallC() {
    return c;
  }
  
  /**
   * Returns the number of boosting iterations, the number of stored weak 
   * learners that were stored
   * @return number of boosting iteration
   */
  public int getSmallT() {
    return t;
  }
  
  /**
   * Returns the number of maximal boosting iteration, the maximal number 
   * of weak learners
   * @return
   */
  public int getT() {
    return T;
  }
  
  /**
   * Returns the error rate of the algorithm, that was approximated on the 
   * training set
   * @return approximated error rate
   */
  public double getComulativeErr() {
    return cumulativeError;
  }
  
  
}
