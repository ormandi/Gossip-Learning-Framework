package gossipLearning.models.clusterer;

import java.util.ArrayList;
import java.util.List;

import peersim.config.Configuration;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.SparseVector;

/**
 * This model is a mergeable K-means clustering algorithm.</br></br>
 *  
 * Using greedy matching over centroidsets.
 * 
 * @author Arpad Berta
 */
public class MergeableKMeansGreedy extends KMeans implements Mergeable<MergeableKMeansGreedy>{

  private static final long serialVersionUID = -5854100193018754177L;
  private static final String PAR_K = "MergeableKMeansGreedy.K";
  private static final String PAR_WSIZE = "MergeableKMeansGreedy.wsize";

  /**
   * Constructs a MergeableKMeansGreedy object. Should not use without init(String prefix) function.
   */
  public MergeableKMeansGreedy() {
    super();
  }
  
  /**
   * Constructs a MergeableKMeansGreedy object with the specified number of cluster centroids and windowsize.
   * @param K the number of centroids
   */
  public MergeableKMeansGreedy(int K, double wSize){
    super(K, wSize);
  }
  
  /**
   * Constructs a MergeableKMeansGreedy object by deep copy cloning the specified MergeableKMeansGreedy object.
   * @param a to be cloned
   */
  public MergeableKMeansGreedy(MergeableKMeansGreedy a){
    super(a);
  }

  /**
   * Deep copy of this object.
   */
  public Object clone(){
    return new MergeableKMeansGreedy(this);
  }

  public void init(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
    centroids = new SparseVector[K];
  }

  public MergeableKMeansGreedy merge(MergeableKMeansGreedy model) {
    boolean hasEmptyCentroidInModel = hasEmptyCentroid(model);
    boolean hasEmptyCentroidInThis = hasEmptyCentroid(this);
    if (hasEmptyCentroidInThis || hasEmptyCentroidInModel) {
      if (hasEmptyCentroidInThis) {
        fillEmptyCentroids(model);
      }
      return this;
    } else {
      MergeableKMeansGreedy mrg = new MergeableKMeansGreedy(this);

      List<SparseVector> centroidSet = new ArrayList<SparseVector>();

      for (SparseVector centroid : mrg.centroids) {
        centroidSet.add(centroid);
      }
      for (SparseVector centroid : model.centroids) {
        centroidSet.add(centroid);
      }

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
      
      for(int i = 0; i < centroidSet.size(); i++) {
        mrg.centroids[i] = centroidSet.get(i);
      }

      return mrg;
    }
  }

}
