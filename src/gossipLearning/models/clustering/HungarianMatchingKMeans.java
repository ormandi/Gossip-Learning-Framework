package gossipLearning.models.clustering;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.HungarianMethod;
import peersim.config.Configuration;

/**
 * This model is a mergeable K-means clustering algorithm.</br></br>
 *  
 * Using Hungarian method to match the centroids.
 * 
 * @author Arpad Berta
 */
public class HungarianMatchingKMeans extends KMeans implements Mergeable {

  private static final long serialVersionUID = 2224933510524588173L;
  private static final String PAR_WSIZE = "HungarianMatchingKMeans.wsize";
  private static final String PAR_K = "HungarianMatchingKMeans.K";

  /**
   * Constructs a HungarianMatchingKMeans object. Should not use without init(String prefix) function.
   */
  public HungarianMatchingKMeans(String prefix) {
    super(prefix);
    this.K = Configuration.getInt(prefix + "." + PAR_K);
    this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
  }

  /**
   * Constructs a HungarianMatchingKMeans object with the specified number of cluster centroids and windowsize.
   * @param K the number of centroids
   */
  public HungarianMatchingKMeans(int K, double wSize) {
    super(K, wSize);
  }
  /**
   * Constructs a HungarianMatchingKMeans object by deep copy cloning the specified HungarianMatchingKMeans object.
   * @param a to be cloned
   */
  public HungarianMatchingKMeans(HungarianMatchingKMeans a) {
    super(a);
  }

  /**
   * Deep copy of this object.
   */
  public Object clone(){
    return new HungarianMatchingKMeans(this);
  }

  @Override
  public Model merge(Model model) {
    HungarianMatchingKMeans m = (HungarianMatchingKMeans)model;
    boolean hasEmptyCentroidInModel = hasEmptyCentroid(m);
    boolean hasEmptyCentroidInThis = hasEmptyCentroid(this);
    if (hasEmptyCentroidInThis || hasEmptyCentroidInModel) {
      if (hasEmptyCentroidInThis) {
        fillEmptyCentroids(m);
      } // if hasEmptyCentroidInModel is true than do nothing
      return this;
    } else {
      
      HungarianMatchingKMeans mkm= new HungarianMatchingKMeans(this);
      return crossover(m, mkm);/**/
    }
  }
  
  /*@Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }*/
  
  protected HungarianMatchingKMeans crossover(HungarianMatchingKMeans model, HungarianMatchingKMeans mkm) {
    HungarianMethod hum = new HungarianMethod(mkm.centroids, model.centroids);
    int[] permutationArray = hum.getPermutationArray();
    for (int i = 0; i < K; i++) {
      double myage = mkm.centroidAge[i];
      double moage = model.centroidAge[permutationArray[i]];
      if (myage+moage != 0) {
        mkm.centroids[i].mul(myage/(myage+moage));
        model.centroids[permutationArray[i]].mul(moage/(myage+moage));
        mkm.centroids[i].add( model.centroids[permutationArray[i]] );
        double tmpMyCentroidAge = (myage * myage/(myage+moage));
        double tmpModelCentroidAge = (moage * moage/(myage+moage));
        mkm.centroidAge[i] = tmpModelCentroidAge+tmpMyCentroidAge;
      }
    }
    return mkm;
  }

}
