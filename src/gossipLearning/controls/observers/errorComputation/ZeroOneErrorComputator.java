package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;

/**
 * Such a kind of error computator that computes the 0-1 error of the latest model only.
 * @author István Hegedűs
 *
 */
public class ZeroOneErrorComputator extends AbstractErrorComputator {

  public ZeroOneErrorComputator(int pid, InstanceHolder eval) {
    super(pid, eval);
  }
  
  /**
   * This function computes the error of the latest model only.
   */
  public double[] computeError(ModelHolder modelHolder, int nodeID) {
    double avgZeroOneErrorOfNodeI = 0.0;
    for (int j = 0; j < eval.size(); j ++) {
      double predictedValue = modelHolder.getModel(modelHolder.size() -1).predict(eval.getInstance(j));
      double expectedValue = eval.getLabel(j);
      avgZeroOneErrorOfNodeI += (expectedValue != predictedValue) ? 1.0 : 0.0;
    }
    avgZeroOneErrorOfNodeI /= eval.size();
    return new double[]{avgZeroOneErrorOfNodeI};
  }
  
}
