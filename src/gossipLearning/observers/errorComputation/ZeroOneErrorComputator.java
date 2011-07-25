package gossipLearning.observers.errorComputation;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Vector;

public class ZeroOneErrorComputator<I, M extends Model<I>> extends AbstractErrorComputator<I, M> {

  public ZeroOneErrorComputator(int pid, Vector<I> instances, Vector<Double> labels) {
    super(pid, instances, labels);
  }
  
  public double[] computeError(ModelHolder<M> modelHolder, int nodeID) {
    double avgZeroOneErrorOfNodeI = 0.0;
    for (int j = 0; j < instances.size(); j ++) {
      I testInstance = instances.get(j);
      //double predictedValue = (Utils.innerProduct(modelHolder.getModel(), testInstance) + modelHolder.getBias() > 0.0) ? 1.0 : -1.0;
      double predictedValue = modelHolder.getModel().predict(testInstance);
      double expectedValue = labels.get(j);
      avgZeroOneErrorOfNodeI += (expectedValue != predictedValue) ? 1.0 : 0.0;
    }
    avgZeroOneErrorOfNodeI /= instances.size();
    return new double[]{avgZeroOneErrorOfNodeI};
  }
  
  public int numberOfComputedErrors() {
    return 1;
  }

}
