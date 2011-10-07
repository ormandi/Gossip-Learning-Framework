package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Map;

/**
 * This class is for computing the voted 0-1 error incrementally.
 * @author István Hegedűs
 *
 */
public class EfficientVotedZeroOneErrorComputator extends AbstractErrorComputator {
  
  /**
   * Constructor for voted 0-1 error computator.
   * @param pid process ID
   * @param eval evaluation set
   */
  public EfficientVotedZeroOneErrorComputator(int pid, InstanceHolder eval) {
    super(pid, eval);
  }
  
  /**
   * Computes the voted 0-1 errors efficiently.
   */
  public double[] computeError(ModelHolder modelHolder, int nodeID) {
    double[] errors = new double[modelHolder.size()];
    int[] numOfPosPreds = new int[eval.size()];
    
    // compute errors
    for (int modelIdx = modelHolder.size() - 1, errorIdx = 0; errorIdx < errors.length; modelIdx --, errorIdx ++) {
      if (modelIdx >= 0) {
        double error = 0.0;
        Model model = modelHolder.getModel(modelIdx);
        
        for (int testIdx = 0; testIdx < eval.size(); testIdx ++) {
          Map<Integer, Double> testInstance = eval.getInstance(testIdx);
          double p = model.predict(testInstance);
          numOfPosPreds[testIdx] += (p == 1.0) ? 1 : 0;
          double pRatio = (double)numOfPosPreds[testIdx] / (errorIdx + 1);
          
          double predictedValue = (pRatio >= 0.5) ? 1.0 : -1.0;
          double expectedValue = eval.getLabel(testIdx);
          error += (expectedValue != predictedValue) ? 1.0 : 0.0;
          
          // DEBUG
          //if (nodeID == 1) {
          //  System.out.println("e: modelIdx=" + modelIdx + ", testID=" + testIdx + ", vP=" + predictedValue + ", pRatio=" + pRatio + ", numOfPosPreds=" + numOfPosPreds[testIdx] + ", numOfModels=" + (errorIdx + 1));
          //}
        }
        error /= eval.size();
        errors[errorIdx] = error;
      } else if (errorIdx > 0) {
        errors[errorIdx] = errors[errorIdx - 1];
      }
    }
    
    return errors;
  }
  
}
