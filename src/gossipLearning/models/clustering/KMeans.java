package gossipLearning.models.clustering;

import java.util.ArrayList;
import java.util.List;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

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
  private static final String PAR_WSIZE = "KMeans.wsize";
  
  protected double wSize;
  protected int K;
  protected double age;
  protected double[] centroidAge = null;
  protected SparseVector[] centroids = null;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public KMeans(String prefix) {
    this.age = 0;
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
    this.centroids = new SparseVector[K];
    this.centroidAge = new double[K];
  }
  
  /**
   * Constructs a KMeans object with the specified number of cluster centroids.
   * @param K the number of centroids
   */
  public KMeans(int K, double wSize) {
    this.age = 0;
    this.K = K;
    this.wSize = wSize;
    this.centroids = new SparseVector[K];
    this.centroidAge = new double[K];
  }
  
  /**
   * Constructs a KMeans object by deep copy cloning the specified KMeans object.
   * @param a to be cloned
   */
  public KMeans(KMeans a) {
    K = a.K;
    wSize = a.wSize;
    this.age = a.age;
    this.centroids = new SparseVector[K];
    if (a.centroids != null) {
      for (int i = 0; i < K; i++) {
        if (!(a.centroids[i] == null)) {
          centroids[i] = a.centroids[i].clone();
        }
      }
    }
    centroidAge = new double[K];
    if (a.centroidAge != null) {
      for (int i = 0; i < centroidAge.length; i++) {
        centroidAge[i] = a.centroidAge[i];
      }
    }
  }
  
  /**
   * Deep copy of this object.
   */
  public KMeans clone() {
    return new KMeans(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    // initialization
    int initIndex;
    if (centroids == null) {
      centroids = new SparseVector[K];
      initIndex = 0;
    } else {
      initIndex = getInitializable(centroids);
    }
     
    if (initIndex != -1) {
      if (!containsCentroid(centroids, instance)) {
        centroids[initIndex] = new SparseVector(instance);
        if(centroidAge == null) {
          centroidAge = new double[K];
        }
        if (centroidAge[initIndex] == 0) {
          centroidAge[initIndex] = 1;
          age++;
        }/**/
      }
    } else {
      // update
      int idx = performClustering(instance); 
      centroidAge[idx]++;
      age++;
      double alpha = (1.0 / wSize);
      centroids[idx].mul(1.0 - alpha);
      centroids[idx].add(instance, alpha);
    }
  }
  
  public void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }
  
  @Override
  public void update(InstanceHolder instances, int epoch, int batchSize) {
    for (int i = 0; i < epoch; i++) {
      update(instances);
    }
  }
  
  @Override
  public void clear() {
    age = 0.0;
    for (int i = 0; i < K; i++) {
      centroidAge[i] = 0.0;
      centroids[i].clear();
    }
  }
  
  /**
   * Returns the index of the first uninitialized cluster centroid.
   * @return the index of uninitialized centroid
   */
  protected int getInitializable(SparseVector[] centroids) {
    for (int i = 0; i < K; i++) {
      if (centroids[i]==null) {
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
  protected boolean containsCentroid(SparseVector[] centroids, SparseVector vector) {
    for (int i = 0; i < K; i++) {
      if(centroids[i] == null) {
        continue;
      } else {
        if (centroids[i].equals(vector)) {
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public double predict(SparseVector instance) {
    return performClustering(instance);
  }
  
  protected int performClustering(SparseVector instance) {
    int idx = -1;
    double minDist = Double.MAX_VALUE;
    double dist;
    for (int i = 0; i < K; i++) {
      // find closest centroid
      if(centroids[i] == null) {
        dist = Double.MAX_VALUE;
      } else {
        dist = centroids[i].euclideanDistance(instance);
      }
      if (dist < minDist) {
        minDist = dist;
        idx = i;
      }
    }
    // if all centroids are uninitialized than get random index 
    if (idx == -1){
      idx = CommonState.r.nextInt(K);
    }
    return idx;
  }
  
  /**
   * This method search for empty centroid in all of the examined model. 
   * @param model
   * @return true: if this or the model has empty centroid
   *         false: if all of the examined centroids are filled
   */
  protected boolean hasEmptyCentroid(KMeans model) {
    for (int i = 0; i < model.centroids.length; i++) {
      if(model.centroids[i] == null)
        return true;
    }
    return false;
  }

  /**
   * Fills empty centroids with K-sized subset of the union of examined models's centroids.
   * @param model
   */
  protected void fillEmptyCentroids(KMeans model) {
    List<SparseVector> centroidSet = new ArrayList<SparseVector>();
    // fill the centroidset with the known centroids
    for (int i = 0; i < this.centroids.length; i++) {
      if(this.centroids[i] != null) {
        centroidSet.add(this.centroids[i]);
      } 
      if(model.centroids[i] != null) {
        if (!this.containsCentroid(this.centroids, model.centroids[i])) {
          centroidSet.add(model.centroids[i]);
        }
      }
    }
    int differenceFromK = centroidSet.size() - K;
    if (differenceFromK > 0) {
      centroidSet = partition(centroidSet);
    } 
    for (int i = 0; i < K; i++) {
      if(centroidSet.size() == 0) {
        return; //than the elements of centroidset is smaller then K, therefore we stop filling now 
      }
      int randomNum = CommonState.r.nextInt(centroidSet.size());
      this.centroids[i] = centroidSet.get(randomNum);
      if(centroidAge == null) {
        centroidAge = new double[K];
      }
      if (centroidAge[i] == 0) {
        centroidAge[i] = 1;
        age++;
      }
      centroidSet.remove(randomNum);
    }
  }
  
  protected List<SparseVector> partition(List<SparseVector> centroidSet) {
    while(centroidSet.size() > K) {
      double minDist = Double.MAX_VALUE;
      int mini = -1;
      int minj = -1;
      // Get the index of the two centroids which have the minimum distance.
      for (int i = 0; i < centroidSet.size(); i++) {
        for (int j = i; j < centroidSet.size(); j++) {
          if (i!=j){
            double dist = centroidSet.get(i).euclideanDistance(centroidSet.get(j));
            if (dist <= minDist) {
              minDist = dist;
              mini = i;
              minj = j;
            }
          }
        }
      }
      // Make a new centroid. This centroid is the avg of the two closest centroid
      SparseVector insert = new SparseVector(centroidSet.get(mini));
      insert.add(centroidSet.get(minj));
      insert.mul(0.5);
      // Remove the old centroids
      if(minj > mini) {
        centroidSet.remove(minj);
        centroidSet.remove(mini);
      } else {
        centroidSet.remove(mini);
        centroidSet.remove(minj);
      }
      // Add the new one
      centroidSet.add(insert);
    }
    return centroidSet;
  }
  
  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public void setAge(double age) {
    this.age = age;
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
  }
  
  public SparseVector[] getCentroids() {
    return centroids;
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
