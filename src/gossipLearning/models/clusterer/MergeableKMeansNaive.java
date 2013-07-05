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
 * Using identical matching.
 * 
 * @author Arpad Berta
 */
public class MergeableKMeansNaive extends KMeans implements Mergeable<MergeableKMeansNaive> {

  private static final long serialVersionUID = -6814021140313963263L;
  private static final String PAR_WSIZE = "MergeableKMeansNaiv.wsize";
  private static final String PAR_K = "MergeableKMeansNaiv.K";

  /**
   * Constructs a MergeableKMeansNaiv object. Should not use without init(String prefix) function.
   */
  public MergeableKMeansNaive() {
    super();
  }

  /**
   * Constructs a MergeableKMeansNaiv object with the specified number of cluster centroids and windowsize.
   * @param K the number of centroids
   */
  public MergeableKMeansNaive(int K, double wSize){
    super(K, wSize);
  }
  /**
   * Constructs a MergeableKMeansNaiv object by deep copy cloning the specified MergeableKMeansNaiv object.
   * @param a to be cloned
   */
  public MergeableKMeansNaive(MergeableKMeansNaive a){
    super(a);
  }
  /**
   * Deep copy of this object.
   */
  public Object clone(){
    return new MergeableKMeansNaive(this);
  }

  public void init(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
    centroids = new SparseVector[K];
  }

  public MergeableKMeansNaive merge(MergeableKMeansNaive model) {
    boolean hasEmptyCentroidInModel = hasEmptyCentroid(model);
    boolean hasEmptyCentroidInThis = hasEmptyCentroid(this);
    if (hasEmptyCentroidInThis || hasEmptyCentroidInModel) {
      if (hasEmptyCentroidInThis) {
        fillEmptyCentroids(model);
      }
      return this;
    } else {
      MergeableKMeansNaive mkm = new MergeableKMeansNaive(this);

      for (int i = 0; i < centroids.length; i++) {
        SparseVector mergesw = new SparseVector(this.centroids[i]);
        mergesw.add( model.centroids[i] );
        mergesw.mul(0.5);
        mkm.centroids[i] = mergesw;
      }

      return mkm;
    }
  }
  
  /**
   * Fills empty centroids with K-sized subset of the union of examined models's centroids.
   * @param model
   */
  private void fillEmptyCentroids(MergeableKMeansNaive model) {
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
  private boolean hasEmptyCentroid(MergeableKMeansNaive model) {
    boolean isUninitialized = false;
    for (int i = 0; i < model.centroids.length; i++) {
      if(model.centroids[i] == null)
        return true;
    }
    return isUninitialized;
  }
}
