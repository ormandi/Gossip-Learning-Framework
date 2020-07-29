package gossipLearning.interfaces.optimizers;

import java.io.Serializable;

import gossipLearning.interfaces.Vector;
import gossipLearning.utils.DenseVector;
import gossipLearning.utils.SparseVector;

public abstract class Optimizer implements Cloneable, Serializable {
  private static final long serialVersionUID = 7205543804349151039L;
  
  public Vector delta;
  public double biasDelta;
  
  public Optimizer() {
    delta = new DenseVector();
    //delta = new SparseVector();
    biasDelta = 0.0;
  }
  
  public Optimizer(String prefix) {
    this();
  }
  
  public Optimizer(Optimizer a) {
    delta = a.delta.clone();
    biasDelta = a.biasDelta;
  }
  
  public abstract Optimizer clone();
  public abstract void delta(double lr, Vector gradient, double biasGradient);
  public abstract Optimizer merge(Optimizer o, double weight);
  public abstract Optimizer add(Optimizer o, double times);
}
