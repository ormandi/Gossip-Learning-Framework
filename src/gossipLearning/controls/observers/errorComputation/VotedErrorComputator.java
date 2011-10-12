package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Map;

/**
 * This class computes the voted error using the specified error function.
 * @author István Hegedűs
 *
 */
public class VotedErrorComputator extends AbstractErrorComputator {
  
  /**
   * Constructor for voted error computator.
   * @param pid process ID
   * @param eval evaluation set
   */
  public VotedErrorComputator(int pid, InstanceHolder eval, ErrorFunction errorFunction) {
    super(pid, eval, errorFunction);
  }
  
  /**
   * Computes the voted errors incrementally.
   */
  public double[] computeError(ModelHolder modelHolder, int nodeID) {
    double[] errors = new double[modelHolder.size()];
    double[] meanOfPredictions = new double[eval.size()];
    
    // compute errors
    for (int modelIdx = modelHolder.size() - 1, errorIdx = 0; errorIdx < errors.length; modelIdx --, errorIdx ++) {
      if (modelIdx >= 0) {
        double error = 0.0;
        Model model = modelHolder.getModel(modelIdx);
        
        for (int testIdx = 0; testIdx < eval.size(); testIdx ++) {
          Map<Integer, Double> testInstance = eval.getInstance(testIdx);
          double p = model.predict(testInstance);
          if (errorIdx > 0) {
            meanOfPredictions[testIdx] += (p - meanOfPredictions[testIdx]) / (errorIdx + 1);
          } else {
            meanOfPredictions[testIdx] = p;
          }
          
          double predictedValue = meanOfPredictions[testIdx];
          double expectedValue = eval.getLabel(testIdx);
          error += errorFunction.computeError(expectedValue, predictedValue);
          
          // DEBUG
          //if (nodeID == 1) {
          //  System.out.println("e: modelIdx=" + modelIdx + ", testID=" + testIdx + ", vP=" + predictedValue + ", pRatio=" + pRatio + ", numOfPosPreds=" + numOfPosPreds[testIdx] + ", numOfModels=" + (errorIdx + 1));
          //}
        }
        error /= eval.size();
        errors[errorIdx] = errorFunction.postProcess(error);
      } else if (errorIdx > 0) {
        errors[errorIdx] = errors[errorIdx - 1];
      }
    }
    return errors;
  }
  
}
