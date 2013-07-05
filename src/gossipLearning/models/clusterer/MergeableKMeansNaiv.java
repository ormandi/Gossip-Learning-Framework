package gossipLearning.models.clusterer;

import peersim.config.Configuration;
import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.SparseVector;
/**
 * This model is a mergeable K-means clustering algorithm.</br></br>
 *  
 * Using identical matching.
 * 
 * @author Arpad Berta
 */
public class MergeableKMeansNaiv extends KMeans implements Mergeable<MergeableKMeansNaiv> {

  private static final long serialVersionUID = -6814021140313963263L;
  private static final String PAR_WSIZE = "MergeableKMeansNaiv.wsize";
  private static final String PAR_K = "MergeableKMeansNaiv.K";

  /**
   * Constructs a MergeableKMeansNaiv object. Should not use without init(String prefix) function.
   */
  public MergeableKMeansNaiv() {
    super();
  }

  /**
   * Constructs a MergeableKMeansNaiv object with the specified number of cluster centroids and windowsize.
   * @param K the number of centroids
   */
  public MergeableKMeansNaiv(int K, double wSize){
    super(K, wSize);
  }
  /**
   * Constructs a MergeableKMeansNaiv object by deep copy cloning the specified MergeableKMeansNaiv object.
   * @param a to be cloned
   */
  public MergeableKMeansNaiv(MergeableKMeansNaiv a){
    super(a);
  }
  /**
   * Deep copy of this object.
   */
  public Object clone(){
    return new MergeableKMeansNaiv(this);
  }

  public void init(String prefix) {
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
    centroids = new SparseVector[K];
  }

  public MergeableKMeansNaiv merge(MergeableKMeansNaiv model) {
    boolean hasEmptyCentroidInModel = hasEmptyCentroid(model);
    boolean hasEmptyCentroidInThis = hasEmptyCentroid(this);
    if (hasEmptyCentroidInThis || hasEmptyCentroidInModel) {
      if (hasEmptyCentroidInThis) {
        fillEmptyCentroids(model);
      }
      return this;
    } else {
      MergeableKMeansNaiv mkm = new MergeableKMeansNaiv(this);
      
      //avg(this.cenroids, model.centroids) with identical permutation
      for (int i = 0; i < centroids.length; i++) {
        mkm.centroids[i].add( model.centroids[i] );
        mkm.centroids[i].mul(0.5);  
      }

      return mkm;
    }
  }
    
}
