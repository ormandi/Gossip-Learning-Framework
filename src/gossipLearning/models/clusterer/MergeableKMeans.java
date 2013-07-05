package gossipLearning.models.clusterer;

import peersim.config.Configuration;
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
		  } // if hasEmptyCentroidInModel is true than do nothing
		  return this;
		} else {
			MergeableKMeans mkm= new MergeableKMeans(this);
			HungarianMethod hum = new HungarianMethod(mkm.centroids, model.centroids);
			int[] permutationArray = hum.getPermutationArray();

			// avg(this, model) with permutation which is get from result of hungarian method
			for (int i = 0; i < this.centroids.length; i++) {
				mkm.centroids[i].add( model.centroids[permutationArray[i]] );
				mkm.centroids[i].mul(0.5);
			}
			
			return mkm;
		}

	}
	
}