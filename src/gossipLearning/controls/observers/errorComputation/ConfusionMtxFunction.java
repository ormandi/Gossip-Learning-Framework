package gossipLearning.controls.observers.errorComputation;

import gossipLearning.utils.SparseVector;

import java.util.Map;
import java.util.TreeMap;

/**
 * This abstract class supports the confusion matrix based measurements 
 * by computing the matrix through the computeError function calls.
 * @note At the end of the postProcess function the matrix should be clear!
 * @author István Hegedűs
 *
 */
public abstract class ConfusionMtxFunction implements ErrorFunction {

  /**
   * Represents the confusion matrix.
   */
  protected Map<Integer, SparseVector> confusionMatrix;
  
  @Override
  public final double computeError(double expected, double predicted) {
    if (expected != (int)expected || predicted != (int)predicted) {
      throw new RuntimeException("The expected and the predicted labels should be integer values: " + expected + " " + predicted);
    }
    if (confusionMatrix == null) {
      confusionMatrix = new TreeMap<Integer, SparseVector>();
    }
    SparseVector row = confusionMatrix.get((int)expected);
    if (row == null) {
      row = new SparseVector();
      confusionMatrix.put((int)expected, row);
    }
    double value = row.get((int)predicted);
    value ++;
    row.put((int)predicted, value);
    return 0.0;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    int numClasses = 0;
    sb.append("e\\p");
    for (int i : confusionMatrix.keySet()) {
      sb.append('\t');
      sb.append(i);
      if (numClasses <= i) {
        numClasses = i + 1;
      }
    }
    sb.append('\n');
    for (int i = 0; i < numClasses; i++) {
      SparseVector v = confusionMatrix.get(i);
      sb.append(i);
      for (int j = 0; j < numClasses; j++) {
        sb.append('\t');
        if (v == null || v.get(j) == 0.0) {
          sb.append(0);
        } else {
          sb.append((int)v.get(j));
        }
      }
      sb.append('\n');
    }
    return sb.toString();
  }

}
