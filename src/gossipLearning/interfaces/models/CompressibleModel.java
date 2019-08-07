package gossipLearning.interfaces.models;

import java.util.Map;

/**
 * A model that supports the simulation of lossy compression.
 */
public interface CompressibleModel extends Model {
  
  /** Saves any data that needs to be compressed into an empty map. */
  void getData(Map<Integer,Double> data);
  
  /** Loads the decompressed data into the model. */
  void setData(Map<Integer,Double> data);
  
  /** Returns a deep copy of this object. */
  CompressibleModel clone();
  
}
