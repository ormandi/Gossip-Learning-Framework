package gossipLearning.models.clusterer;

import gossipLearning.interfaces.Model;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This model is a simple K-means clustering algorithm.</br></br>
 * Using running average method with approximately 100 window size for 
 * updating cluster centroids.
 * 
 * @author István Hegedűs
 *
 */
public class KMeans implements Model {
  private static final long serialVersionUID = -1382541535383273679L;
  
  private static final String PAR_K = "KMeans.K";
  private static final double wSize = 100.0;
  
  private int K;
  /** @hidden*/
  private SparseVector[] centroids = null;
  private boolean[] isInitialized = null;
  
  /**
   * Constructs a KMeans object. Should not use without init(String prefix) function.
   */
  public KMeans() {
  }
  
  /**
   * Constructs a KMeans object with the specified number of cluster centroids.
   * @param K the number of centroids
   */
  public KMeans(int K) {
    this();
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

  @Override
  public void init(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    centroids = new SparseVector[K];
    isInitialized = new boolean[K];
    for (int i = 0; i < K; i++) {
      centroids[i] = new SparseVector();
      isInitialized[i] = false;
    }
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

}
