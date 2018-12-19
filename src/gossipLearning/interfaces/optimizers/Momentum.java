package gossipLearning.interfaces.optimizers;

import peersim.config.Configuration;
import gossipLearning.utils.SparseVector;

public class Momentum extends Optimizer {
  private static final long serialVersionUID = 7141942220197077908L;
  private static final String PAR_ALPHA = "alpha";
  
  protected final double alpha;
  protected SparseVector momentum;
  protected double biasMomentum;
  
  public Momentum() {
    alpha = 0.9;
    momentum = new SparseVector();
    biasMomentum = 0.0;
  }
  
  public Momentum(String prefix) {
    super(prefix);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
    momentum = new SparseVector();
    biasMomentum = 0.0;
  }
  
  public Momentum(Momentum a) {
    super(a);
    alpha = a.alpha;
    momentum = (SparseVector)a.momentum.clone();
    biasMomentum = a.biasMomentum;
  }
  
  @Override
  public Object clone() {
    return new Momentum(this);
  }

  @Override
  public void delta(double lr, SparseVector gradient, double biasGradient) {
    momentum.mul(alpha).add(gradient, lr);
    biasMomentum = (biasMomentum * alpha) + (lr * biasGradient);
    delta.set(momentum);
    biasDelta = biasMomentum;
  }

}
