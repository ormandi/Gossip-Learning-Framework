package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;

/**
 * This abstract class describes the skeleton of error computators that
 * can be used in our learning framework.
 * @author István Hegedűs
 *
 */
public abstract class AbstractErrorComputator {
  /**
   * Evaluation set
   * @hidden
   */
  protected final InstanceHolder eval;
  /**
   * Type of computable error
   * @hidden
   */
  protected final ErrorFunction errorFunction;
  
  /**
   * Constructor for error computator that stores the specified parameters.
   * @param pid process ID
   * @param eval evaluation set
   * @param errorFunction type of computable error
   */
  public AbstractErrorComputator(InstanceHolder eval, ErrorFunction errorFunction) {
    this.eval = eval;
    this.errorFunction = errorFunction;
  }
  
  /**
   * Computes the voted error on the specified node as nodeID based on the specified modelHolder.
   * @param modelHolder
   * @return the array where the index represents the number of votes
   */
  public abstract double[] computeError(ModelHolder modelHolder);

}
