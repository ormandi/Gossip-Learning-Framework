package gossipLearning.controls.observers;

import gossipLearning.utils.Utils;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.Network;

/**
 * Such a kind of prediction observer where the number of tested models can be defined. 
 * @author István Hegedűs
 *
 */
public class SamplingBasedPredictionObserver extends PredictionObserver {
  private static final String PAR_SS = "samples";
  
  /**
   * Number of selected nodes to compute for prediction
   */
  protected final int samples;
  /** @hidden */
  protected final Random r;
  protected int[] indices; 

  /**
   * Creates an object from this class.
   * @param prefix configurations.
   * @throws Exception from super class.
   */
  public SamplingBasedPredictionObserver(String prefix) throws Exception{
    super(prefix);
    samples = Configuration.getInt(prefix + "." + PAR_SS);
    long seed = Configuration.getLong("random.seed");
    r = new Random(seed);
    indices = new int[Network.size()];
    for (int i = 0; i < Network.size(); i ++) {
      indices[i] = i;
    }
  }
  
  /**
   * Generates a predefined sized set of node indices for evaluating the stored model.
   */
  protected Set<Integer> generateIndices() {
    if (samples >= g.size() || samples < 1) {
      return super.generateIndices();
    }
    TreeSet<Integer> result = new TreeSet<Integer>();
    Utils.arrayShuffle(r, indices);
    for (int i = 0; i < samples; i++) {
      result.add(indices[i]);
    }
    return result; 
  }
}
