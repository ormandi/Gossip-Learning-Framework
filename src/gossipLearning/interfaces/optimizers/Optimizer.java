package gossipLearning.interfaces.optimizers;

import gossipLearning.utils.SparseVector;

import java.io.Serializable;

public abstract class Optimizer implements Cloneable, Serializable {
  private static final long serialVersionUID = 7205543804349151039L;
  
  public SparseVector delta;
  public double biasDelta;
  
  public Optimizer() {
    delta = new SparseVector();
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
  public abstract void delta(double lr, SparseVector gradient, double biasGradient);
  public abstract Optimizer merge(Optimizer o, double weight);
  public abstract Optimizer add(Optimizer o, double times);
}
