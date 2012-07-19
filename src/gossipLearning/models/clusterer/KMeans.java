package gossipLearning.models.clusterer;

import gossipLearning.interfaces.Model;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class KMeans implements Model {
  private static final long serialVersionUID = -1382541535383273679L;
  
  private static final String PAR_K = "KMeans.K";
  
  private int K;
  private SparseVector[] centroids = null;
  private boolean[] isInitialized = null;
  private double age;
  
  public KMeans() {
    age = 0.0;
  }
  
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
  
  public KMeans(KMeans a) {
    K = a.K;
    age = a.age;
    if (a.centroids != null) {
      centroids = new SparseVector[K];
      isInitialized = new boolean[K];
      for (int i = 0; i < K; i++) {
        centroids[i] = (SparseVector)a.centroids[i].clone();
        isInitialized[i] = a.isInitialized[i];
      }
    }
  }
  
  public Object clone() {
    return new KMeans(this);
  }

  @Override
  public void init(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    age = 0.0;
    centroids = new SparseVector[K];
    isInitialized = new boolean[K];
    for (int i = 0; i < K; i++) {
      centroids[i] = new SparseVector();
      isInitialized[i] = false;
    }
  }
  
  private int getInitializable() {
    for (int i = 0; i < K; i++) {
      if (!isInitialized[i]) {
        return i;
      }
    }
    return -1;
  }
  
  private boolean conrainsCentroid(SparseVector vector) {
    for (int i = 0; i < K; i++) {
      if (centroids[i].equals(vector)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void update(SparseVector instance, double label) {
    SparseVector vector = (SparseVector)instance.clone();
    
    // initialization
    int initIndex = getInitializable();
    if (initIndex != -1) {
      if (!conrainsCentroid(vector)) {
        centroids[initIndex].add(vector);
        isInitialized[initIndex] = true;
      }
      
    }
    int idx = (int)predict(vector);
    
    // update
    double wSize = 100;
    centroids[idx].mul(1.0 - (2.0 / wSize));
    centroids[idx].add(vector.mul(2.0 / wSize));
    
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
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < K; i++) {
      sb.append(centroids[i].toString());
      sb.append('\n');
    }
    return sb.toString();
  }

}
