package gossipLearning.interfaces.optimizers;

import gossipLearning.utils.SparseVector;

public class GD extends Optimizer {
  private static final long serialVersionUID = 3784331513723993996L;

  public GD() {
    super();
  }
  
  public GD(String prefix) {
    super(prefix);
  }
  
  public GD(GD a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new GD(this);
  }
  
  @Override
  public void delta(double lr, SparseVector gradient, double biasGradient) {
    delta.set(gradient).mul(lr);
    biasDelta = lr * biasGradient;
  }

}
