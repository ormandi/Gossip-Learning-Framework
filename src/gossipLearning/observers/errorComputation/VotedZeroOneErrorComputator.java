package gossipLearning.observers.errorComputation;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ModelQueueHolder;

import java.util.Vector;


public class VotedZeroOneErrorComputator<I> extends AbstractErrorComputator<I> {
  
  public VotedZeroOneErrorComputator(int pid, Vector<I> instances, Vector<Double> labels) {
    super(pid, instances, labels);
  }

  @SuppressWarnings("unchecked")
  public double[] computeError(ModelHolder<I> mH, int nodeID) {
    if (mH instanceof ModelQueueHolder) {
      ModelQueueHolder<I> modelHolder = (ModelQueueHolder<I>) mH;
      double avgZeroOneErrorOfNodeI = 0.0;
      
      for (int j = 0; j < instances.size(); j ++) {
        I testInstance = instances.get(j);
        double pClassRatio = 0.0;
        for (Model<I> model : modelHolder.getModelQueue()) {
          //double p = (Utils.innerProduct(model.getModel(), testInstance) + model.getBias() > 0.0) ? 1.0 : -1.0;
          double p = model.predict(testInstance);
          pClassRatio += (p == 1.0) ? 1.0 : 0.0;
        }
        pClassRatio /= (double) modelHolder.getModelQueue().size();
        double predictedValue = (pClassRatio >= 0.5) ? 1.0 : -1.0;
        double expectedValue = labels.get(j);
        avgZeroOneErrorOfNodeI += (expectedValue != predictedValue) ? 1.0 : 0.0;
        
        // DEBUG
        //if (nodeID == 1) {
        //  System.out.println("v: textIdx=" + j + ", vP=" + predictedValue);
        //}
      }
      avgZeroOneErrorOfNodeI /= instances.size();
      return new double[]{avgZeroOneErrorOfNodeI};
    } else {
      throw new RuntimeException("Using VotedZeroOneErrorComputator requires *ModelQueueHolder* parameter.");
    }
  }
  
  public int numberOfComputedErrors() {
    return 1;
  }

}
