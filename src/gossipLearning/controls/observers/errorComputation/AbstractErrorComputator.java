package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;

public abstract class AbstractErrorComputator {
  protected final int pid;
  protected final InstanceHolder eval;
  
  public AbstractErrorComputator(int pid, InstanceHolder eval) {
    this.pid = pid;
    this.eval = eval;
  }
  public abstract double[] computeError(ModelHolder modelHolder, int nodeID);
  public abstract int numberOfComputedErrors();

}
