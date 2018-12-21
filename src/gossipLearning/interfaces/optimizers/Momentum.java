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
    momentum.mul(alpha).add(gradient, lr * (1.0 - alpha));
    biasMomentum = (biasMomentum * alpha) + (lr * biasGradient * (1.0 - alpha));
    delta.set(momentum);
    biasDelta = biasMomentum;
  }
  
  @Override
  public Optimizer merge(Optimizer o, double weight) {
    Momentum m = (Momentum)o;
    momentum.mul(1.0 - weight).add(m.momentum, weight);
    biasMomentum += (m.biasMomentum - biasMomentum) * weight;
    return this;
  }
  
  @Override
  public Optimizer add(Optimizer o, double times) {
    Momentum m = (Momentum)o;
    momentum.add(m.momentum, times);
    biasMomentum += m.biasMomentum * times;
    return this;
  }

}
