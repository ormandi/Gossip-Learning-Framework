package gossipLearning.models.clusterer;

import java.util.LinkedList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;

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

      List<SparseVector> centroidSet = new LinkedList<SparseVector>();

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

        for (int i = 0; i < centroidSet.size(); i++) {
          for (int j = 0; j < centroidSet.size(); j++) {
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
        
        SparseVector insert = new SparseVector(centroidSet.get(mini));
        SparseVector added = new SparseVector(centroidSet.get(minj));
        insert.add(added);
        insert.mul(0.5);
        
        if(minj > mini) {
          centroidSet.remove(minj);
          centroidSet.remove(mini);
        } else {
          centroidSet.remove(mini);
          centroidSet.remove(minj);
        }
        
        centroidSet.add(insert);
      }
      
      for(int i = 0; i < centroidSet.size(); i++) {
        mrg.centroids[i] = centroidSet.get(i);
      }

      return mrg;
    }
  }
  
  /**
   * Fills empty centroids with K-sized subset of the union of examined models's centroids.
   * @param model
   */
  private void fillEmptyCentroids(MergeableKMeansGreedy model) {
    List<SparseVector> centroidSet = new LinkedList<SparseVector>();
    for (int i = 0; i < this.centroids.length; i++) {
      if(!(this.centroids[i] == null)) {
        centroidSet.add(this.centroids[i]);
      }
      if(!(model.centroids[i] == null)) {
        if (!this.containsCentroid(model.centroids[i])) {
          centroidSet.add(model.centroids[i]);
        }
      } 
    }
    for (int i = 0; i < K; i++) {
      if(centroidSet.size() == 0) {
        return;
      }
      int min = 0; 
      int max = centroidSet.size()-1;
      int randomNum = CommonState.r.nextInt(max - min + 1) + min;
      this.centroids[i] = centroidSet.get(randomNum);
      centroidSet.remove(randomNum);
    }
  }
  
  /**
   * This method search for empty centroid in all of the examined model. 
   * @param model
   * @return true: if this or the model has empty centroid
   *         false: if all of the examined centroids are filled
   */
  private boolean hasEmptyCentroid(MergeableKMeansGreedy model) {
    boolean isUninitialized = false;
    for (int i = 0; i < model.centroids.length; i++) {
      if(model.centroids[i] == null)
        return true;
    }
    return isUninitialized;
  }

}
