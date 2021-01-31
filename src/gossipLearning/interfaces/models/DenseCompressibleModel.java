package gossipLearning.interfaces.models;

/**
 * A model that supports the simulation of lossy compression.
 * Optimized for dense models.
 */
public interface DenseCompressibleModel extends Model {
  
  /** Saves any data that needs to be compressed into an array. */
  double[] getData();
  
  /** Loads the decompressed data into the model. */
  void setData(double[] data);
  
  /** Returns a deep copy of this object. */
  DenseCompressibleModel clone();
  
}
