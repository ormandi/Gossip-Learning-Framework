package gossipLearning.models.learning.boosting.weakLearners;

import gossipLearning.interfaces.models.WeakLearner;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import peersim.config.Configuration;

/**
 * This class represents a sigmoid based stump learner for solving the problem 
 * of multiple labeled classification. It learns the index of the feature and 
 * the best cut on this feature that mostly separates the classes to each other.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>SigmoidStumpLearner.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 *
 */
public class SigmoidStumpLearner extends WeakLearner {
  private static final long serialVersionUID = 4026037688835333121L;
  
  private static final String PAR_LAMBDA = "SigmoidStumpLearner.lambda";
  
  protected double age;
  protected int bestIndex;
  private double lambda;
  private Random r;
  
  protected Map<Integer, double[]> vs; // sparse for null
  protected Map<Integer, Double> cs; // sparse for 0.1
  protected Map<Integer, Double> ds; // sparse for 0.0
  protected Map<Integer, Double> edges; // sparse for 0.0
  
  private int numberOfClasses;
  private long seed;
  
  private static long c;
  
  /**
   * Constructs an initially learner.
   */
  public SigmoidStumpLearner() {
    age = 1;
    bestIndex = 0;
    lambda = 0.001;
    numberOfClasses = 2;
    vs = new HashMap<Integer, double[]>();
    cs = new HashMap<Integer, Double>();
    ds = new HashMap<Integer, Double>();
    edges = new HashMap<Integer, Double>();
  }
  
  /**
   * Deep copy constructor.
   * @param a to copy
   */
  private SigmoidStumpLearner(SigmoidStumpLearner a) {
    this.age = a.age;
    this.lambda = a.lambda;
    this.numberOfClasses = a.numberOfClasses;
    this.bestIndex = a.bestIndex;
    this.alpha = a.alpha;
    this.seed = a.seed;
    r = new Random(seed | c++);
    this.vs = new HashMap<Integer, double[]>();
    this.cs = new HashMap<Integer, Double>();
    this.ds = new HashMap<Integer, Double>();
    this.edges = new HashMap<Integer, Double>();
    for (int i : a.vs.keySet()) {
      double[] array = a.vs.get(i);
      if (array != null) {
        double[] tmp = new double[array.length];
        for (int j = 0; j < array.length; j++) {
          tmp[j] = array[j];
        }
        this.vs.put(i, tmp);
      } else {
        this.vs.put(i, array);
      }
    }
    for (int i : a.cs.keySet()) {
      this.cs.put(i, a.cs.get(i).doubleValue());
    }
    for (int i : a.ds.keySet()) {
      this.ds.put(i, a.ds.get(i).doubleValue());
    }
    for (int i : a.edges.keySet()) {
      this.edges.put(i, a.edges.get(i).doubleValue());
    }
  }
  
  /**
   * Deep copy.
   */
  public Object clone(){
    return new SigmoidStumpLearner(this);
  }
  
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    seed = Configuration.getLong("random.seed");
    r = new Random(seed | c++);
  }
  
  /**
   * In this implementation the index computation of the best feature works in online way.
   */
  @Override
  public void update(final SparseVector instance, final double label, final double[] weigths) {
    age ++;
    // compute nu
    //double nu = 1.0 / (double) (age * lambda); // regularized
    
    for (VectorEntry e : instance){
      // getting sparse values
      Double cValueD = cs.get(e.index);
      double cj = (cValueD != null) ? cValueD.doubleValue() : 0.1;
      Double dValueD = ds.get(e.index);
      double dj = (dValueD != null) ? dValueD.doubleValue() : 0.0;
      double[] vsArray = vs.get(e.index);
      if (vsArray == null){
        vsArray = initVJ();
        vs.put(e.index, vsArray);
      }
      double xj = e.value;
    
      // compute edge for jth index
      Double edgejD = edges.get(e.index);
      double edgej = (edgejD == null) ? 0.0 : edgejD.doubleValue();
      bestIndex = e.index;
      double[] predictions = distributionForInstance(instance);
      
      // update jth edge
      double edgeDelta = 0.0;
      for (int l = 0; l < numberOfClasses; l++){
        double yl = (label == l) ? 1.0 : -1.0;
        double pl = (predictions[l] >= 0.0) ? 1.0 : -1.0;
        edgeDelta += (pl == yl) ? weigths[l] : -weigths[l];
      }
      edges.put(e.index, edgej + edgeDelta);
    
      // computing sigmoid value and partial derivation of sigmoid
      double sigmoid = sigmoid(xj, cj, dj);
      double scaledSigmoid = 2.0 * sigmoid - 1.0;
      double partialSigmoid = sigmoid * (1.0 - sigmoid); 
      
      double gradcj = 0.0;
      double graddj = 0.0;
      for (int l = 0; l < numberOfClasses; l++){
        double yl = (label == l) ? 1.0 : -1.0;
        double expLoss = Math.exp(-1.0 * vsArray[l] * scaledSigmoid * yl);
        // summing up c
        gradcj -= expLoss * weigths[l] * vsArray[l] * yl * 2.0 * xj * partialSigmoid;
        // summing up d
        graddj -= expLoss * weigths[l] * vsArray[l] * yl * 2.0 * partialSigmoid;
        // update v_jl
        //vsArray[l] = (1.0 - 1.0 / age) * vsArray[l] + nu * expLoss * weigths[l] * scaledSigmoid * yl; // regularized
        vsArray[l] += (1.0 / age) * expLoss * weigths[l] * scaledSigmoid * yl;
      }
      
      // update c and d
      //cj = (1.0 - 1.0 / age) * cj - nu * gradcj; // regularized
      cj -= (1.0 / age) * gradcj;
      cs.put(e.index, cj);
      //dj = (1.0 - 1.0 / age) * dj - nu * graddj; // regularized
      dj -= (1.0 / age) * graddj;
      ds.put(e.index, dj);
      vs.put(e.index, Utils.normalize(vsArray));
    }
    
    // finding best index based on edges
    double actualBestValue = Double.NEGATIVE_INFINITY;
    for (int i : edges.keySet()) {
      double actualEdge = edges.get(i).doubleValue();
      if (actualEdge > actualBestValue) {
        actualBestValue = actualEdge;
        bestIndex = i;
      }
    }
  }
  
  /**
   * This method normalizes the length of the result array to one.
   */
  private double[] distribution;
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    Double xjd = instance.get(bestIndex);
    double xj = (xjd == null) ? 0.0 : xjd.doubleValue();
    Double cjd = cs.get(bestIndex);
    double cj = (cjd == null) ? 0.1 : cjd.doubleValue();
    Double djd = ds.get(bestIndex);
    double dj = (djd == null) ? 0.0 : djd.doubleValue();
    double[] vj = vs.get(bestIndex);
    if (vj == null){
      vj = initVJ();
    }
    
    double sigmoid = 2.0 * sigmoid(xj, cj, dj) - 1.0;
    if (distribution == null) {
      distribution = new double[numberOfClasses];
    }
    for (int i = 0; i < numberOfClasses; i++){
      //distribution[i] = vj[i] * sigmoid;
      distribution[i] = (vj[i] < 0 ? -1.0 : 1.0) * sigmoid;
    }
    return Utils.normalize(distribution);
  }
  
  /**
   * Initializes a number of class labels sized vector uniform randomly from {-1.0,1.0} 
   * set and normalizes the length to one.
   * @return
   */
  protected double[] initVJ(){
    double[] vj = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++){
      //vj[i] = CommonState.r.nextBoolean() ? 1.0 : -1.0;
      vj[i] = r.nextBoolean() ? 1.0 : -1.0;
    }
    return Utils.normalize(vj);
  }
  
  /**
   * Returns the sigoid value of the specified parameters using this formula
   *  1 / (1 + exp(-c*x - d)).
   * @param value value of x variable
   * @param c slope
   * @param d offset
   * @return sigmoid value
   */
  private double sigmoid(double value, double c, double d) {
    return 1.0 / (1.0 + Math.exp(-c*value - d));
  }
  
  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses < 2 || numberOfClasses == Integer.MAX_VALUE) {
      throw new RuntimeException("This class can handle classification tasks only!");
    }
    this.numberOfClasses = numberOfClasses; 
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("SigmoidL[Alpha=" + alpha);
    sb.append("\tEdge=" + edges.get(bestIndex));
    sb.append("\tAge=" + age);
    sb.append("\tIndex=" + bestIndex);
    sb.append("\tc=" + cs.get(bestIndex));
    sb.append("\td=" + ds.get(bestIndex) + "\tv=");
    double[] v = vs.get(bestIndex);
    if (v != null) {
      for (int i = 0; i < v.length; i++) {
        sb.append(" " + v[i]);
      }
    }
    sb.append("]");
    return sb.toString();
  }
  
}
