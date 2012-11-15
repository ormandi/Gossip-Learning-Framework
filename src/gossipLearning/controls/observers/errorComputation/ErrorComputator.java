package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;

/**
 * This class computes the error of the latest model only using the specified 
 * error function class.
 * @author István Hegedűs
 *
 */
public class ErrorComputator extends AbstractErrorComputator {

  /**
   * Constructor for error computator that stores the specified parameters.
   * @param pid process ID
   * @param eval evaluation set
   * @param errorFunction type of computable error
   */
  public ErrorComputator(InstanceHolder eval, ErrorFunction errorFunction) {
    super(eval, errorFunction);
  }
  
  /**
   * This function computes the error of the latest model only.
   */
  public double[] computeError(ModelHolder modelHolder) {
    double meanErrorOfNodeI = 0.0;
    for (int j = 0; j < eval.size(); j ++) {
      double predictedValue = modelHolder.size() <= 0 ? 0.0 : modelHolder.getModel(modelHolder.size() -1).predict(eval.getInstance(j));
      double expectedValue = eval.getLabel(j);
      meanErrorOfNodeI += errorFunction.computeError(expectedValue, predictedValue);
    }
    meanErrorOfNodeI /= eval.size();
    meanErrorOfNodeI = errorFunction.postProcess(meanErrorOfNodeI);
    return new double[]{meanErrorOfNodeI};
  }
  
}
