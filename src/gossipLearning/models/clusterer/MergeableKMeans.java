package gossipLearning.models.clusterer;


import java.util.LinkedList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.HungarianMethod;
import gossipLearning.utils.SparseVector;

/**
 * This model is a mergeable K-means clustering algorithm.</br></br>
 *  
 * Using Hungarian method to match the centroids.
 * 
 * @author Arpad Berta
 */
public class MergeableKMeans extends KMeans implements Mergeable<MergeableKMeans> {

	private static final long serialVersionUID = 2224933510524588173L;
	private static final String PAR_WSIZE = "MergeableKMeans.wsize";
	private static final String PAR_K = "MergeableKMeans.K";

	/**
   * Constructs a MergeableKMeans object. Should not use without init(String prefix) function.
   */
	public MergeableKMeans() {
		super();
	}

	/**
   * Constructs a MergeableKMeans object with the specified number of cluster centroids and windowsize.
   * @param K the number of centroids
   */
	public MergeableKMeans(int K, double wSize) {
		super(K, wSize);
	}
	 /**
   * Constructs a MergeableKMeans object by deep copy cloning the specified MergeableKMeans object.
   * @param a to be cloned
   */
	public MergeableKMeans(MergeableKMeans a) {
		super(a);
	}

	 /**
   * Deep copy of this object.
   */
	public Object clone(){
		return new MergeableKMeans(this);
	}

	public void init(String prefix) {
		this.K = Configuration.getInt(prefix + "." + PAR_K);
		this.wSize = Configuration.getDouble(prefix + "." + PAR_WSIZE);
		centroids = new SparseVector[K];
	}

	@Override
	public MergeableKMeans merge(MergeableKMeans model) {
	  boolean hasEmptyCentroidInModel = hasEmptyCentroid(model);
	  boolean hasEmptyCentroidInThis = hasEmptyCentroid(this);
		if (hasEmptyCentroidInThis || hasEmptyCentroidInModel) {
		  if (hasEmptyCentroidInThis) {
	      fillEmptyCentroids(model);
		  }
		  return this;
		} else {
			MergeableKMeans mkm= new MergeableKMeans(this);
			HungarianMethod hum = new HungarianMethod(this.centroids, model.centroids);
			int[] permutationArray = hum.getPermutationArray();

			for (int i = 0; i < this.centroids.length; i++) {
				SparseVector mergecentroid = new SparseVector(this.centroids[i]);
				mergecentroid.add( model.centroids[permutationArray[i]] );
				mergecentroid.mul(0.5);
				mkm.centroids[i] = mergecentroid;	
			}

			return mkm;
		}

	}
	
	/**
   * Fills empty centroids with K-sized subset of the union of examined models's centroids.
   * @param model
   */
  private void fillEmptyCentroids(MergeableKMeans model) {
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
  private boolean hasEmptyCentroid(MergeableKMeans model) {
    boolean isUninitialized = false;
    for (int i = 0; i < model.centroids.length; i++) {
      if(model.centroids[i] == null)
        return true;
    }
    return isUninitialized;
  }
}