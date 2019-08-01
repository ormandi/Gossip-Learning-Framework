package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.interfaces.models.CompressibleModel;
import gossipLearning.models.learning.mergeable.MergeablePegasos;

/**
 * Compressible version of the Pegasos algorithm.
 */
public class CompressiblePegasos extends MergeablePegasos implements CompressibleModel {
  
  public CompressiblePegasos(String prefix) {
    super(prefix);
  }
  
  /**
   * Returns a new CompressiblePegasos object that initializes its variables with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected CompressiblePegasos(CompressiblePegasos a) {
    super(a);
  }
  
  @Override
  public CompressiblePegasos clone() {
    return new CompressiblePegasos(this);
  }

}
