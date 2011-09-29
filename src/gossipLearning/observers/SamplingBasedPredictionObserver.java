package gossipLearning.observers;

import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * Such a kind of prediction observer where the number of tested models can be defined. 
 * @author Istvan Hegedus
 *
 * @param <I> - the type of the instance.
 */
public class SamplingBasedPredictionObserver<I> extends PredictionObserver<I> {
  private static final String PAR_SS = "samples";
  protected final int samples;

  /**
   * Creates an object from this class.
   * @param prefix - configurations.
   * @throws Exception - from super class.
   */
  public SamplingBasedPredictionObserver(String prefix) throws Exception{
    super(prefix);
    samples = Configuration.getInt(prefix + "." + PAR_SS);
  }
  
  /**
   * Generates a predefined sized set of node indices for testing the stored model.
   */
  protected Set<Integer> generateIndices() {
    TreeSet<Integer> indices = new TreeSet<Integer>();
    while (indices.size() < samples) {
      indices.add(CommonState.r.nextInt(g.size()));
    }
    return indices;
  }
}
