package gossipLearning.observers.errorComputation;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Vector;

public abstract class AbstractErrorComputator<I, M extends Model<I>> {
  protected final int pid;
  protected final Vector<I> instances;
  protected final Vector<Double> labels;
  
  public AbstractErrorComputator(int pid, Vector<I> instances, Vector<Double> labels) {
    this.pid = pid;
    this.instances = instances;
    this.labels = labels;
  }
  public abstract double[] computeError(ModelHolder<M> modelHolder, int nodeID);
  public abstract int numberOfComputedErrors();

}
