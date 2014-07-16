package gossipLearning.models.clustering;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This model is a simple K-means clustering algorithm.</br></br>
 * Using running average method with approximately 100 window size for 
 * updating cluster centroids.
 * br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>KMeans.K - the number of clusters</li>
 * </ul>
 * @author István Hegedűs
 *
 */
public class KMeans implements LearningModel {
  private static final long serialVersionUID = -1382541535383273679L;
  
  private static final String PAR_K = "KMeans.K";
  private static final double wSize = 1000.0;
  
  private final int K;
  private double age;
  /** @hidden*/
  private SparseVector[] centroids = null;
  private boolean[] isInitialized = null;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public KMeans(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    centroids = new SparseVector[K];
    isInitialized = new boolean[K];
    for (int i = 0; i < K; i++) {
      centroids[i] = new SparseVector();
      isInitialized[i] = false;
    }
  }
  
  /**
   * Constructs a KMeans object with the specified number of cluster centroids.
   * @param K the number of centroids
   */
  public KMeans(int K) {
    this.K = K;
    centroids = new SparseVector[K];
    isInitialized = new boolean[K];
    for (int i = 0; i < K; i++) {
      centroids[i] = new SparseVector();
      isInitialized[i] = false;
    }
  }
  
  /**
   * Constructs a KMeans object by deep copy cloning the specified KMeans object.
   * @param a to be cloned
   */
  public KMeans(KMeans a) {
    K = a.K;
    if (a.centroids != null) {
      centroids = new SparseVector[K];
      isInitialized = new boolean[K];
      for (int i = 0; i < K; i++) {
        centroids[i] = (SparseVector)a.centroids[i].clone();
        isInitialized[i] = a.isInitialized[i];
      }
    }
  }
  
  /**
   * Deep copy of this object.
   */
  public Object clone() {
    return new KMeans(this);
  }

  /**
   * Returns the index of the first uninitialized cluster centroid.
   * @return the index of uninitialized centroid
   */
  private int getInitializable() {
    for (int i = 0; i < K; i++) {
      if (!isInitialized[i]) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Returns true if the specified vector is a cluster centroid.
   * @param vector to find
   * @return
   */
  private boolean containsCentroid(SparseVector vector) {
    for (int i = 0; i < K; i++) {
      if (centroids[i].equals(vector)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    // initialization
    int initIndex = getInitializable();
    if (initIndex != -1) {
      if (!containsCentroid(instance)) {
        centroids[initIndex].add(instance);
        isInitialized[initIndex] = true;
      }
    }
    
    // update
    int idx = (int)predict(instance);
    centroids[idx].mul(1.0 - (2.0 / wSize));
    centroids[idx].add(instance, 2.0 / wSize);
  }

  @Override
  public double predict(SparseVector instance) {
    int idx = -1;
    double minDist = Double.MAX_VALUE;
    double dist;
    for (int i = 0; i < K; i++) {
      // find closest centroid
      dist = centroids[i].euclideanDistance(instance);
      if (dist < minDist) {
        minDist = dist;
        idx = i;
      }
    }
    return idx;
  }

  @Override
  public int getNumberOfClasses() {
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  /**
   * Prints the cluster centroids (SparseVector toString) separated by new lines.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < K; i++) {
      sb.append(centroids[i].toString());
      sb.append('\n');
    }
    return sb.toString();
  }
  
  public void setCentroids(SparseVector[] centroids) {
    for (int i = 0; i < K; i++) {
      this.centroids[i] = new SparseVector(centroids[i]);
    }
  }

}
