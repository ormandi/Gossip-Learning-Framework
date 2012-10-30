package gossipLearning.controls.observers.errorComputation;

import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;

/**
 * This class can be used for computing the purity measurement 
 * based on the confusion matrix.
 * @author István Hegedűs
 *
 */
public class Purity extends ConfusionMtxFunction {

  @Override
  public double postProcess(double meanError) {
    double purity = 0.0;
    double N = 0.0;
    double max;
    SparseVector v;
    for (int i : confusionMatrix.keySet()) {
      max = 0.0;
      v = confusionMatrix.get(i);
      for (VectorEntry e : v) {
        if (max < e.value) {
          max = e.value;
        }
        N += e.value;
      }
      v.clear();
      purity += max;
    }
    return N == 0.0 ? 0.0 : purity / N;
  }

}
