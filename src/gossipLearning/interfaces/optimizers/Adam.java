package gossipLearning.interfaces.optimizers;

import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class Adam extends Momentum {
  private static final long serialVersionUID = -4874780111488235358L;
  private static final String PAR_BETA = "beta";
  
  protected final double beta;
  protected SparseVector momentum2;
  protected double biasMomentum2;
  
  public Adam() {
    super();
    beta = 0.999;
    momentum2 = new SparseVector();
    biasMomentum2 = 0.0;
  }
  
  public Adam(String prefix) {
    super(prefix);
    beta = Configuration.getDouble(prefix + "." + PAR_BETA);
    momentum2 = new SparseVector();
    biasMomentum2 = 0.0;
  }
  
  public Adam(Adam a) {
    super(a);
    beta = a.beta;
    momentum2 = (SparseVector)a.momentum2.clone();
    biasMomentum2 = a.biasMomentum2;
  }
  
  @Override
  public Object clone() {
    return new Adam(this);
  }
  
  @Override
  public void delta(double lr, SparseVector gradient, double biasGradient) {
    momentum.mul(alpha).add(gradient, 1.0 - alpha);
    biasMomentum = (biasMomentum * alpha) + ((1.0 - alpha) * biasGradient);
    
    momentum2.mul(beta).add(gradient.powerTo(2.0), 1.0 - beta);
    gradient.sqrt();
    biasMomentum2 = (biasMomentum2 * alpha) + ((1.0 - alpha) * biasGradient * biasGradient);
    
    momentum2.sqrt();
    delta.set(momentum).div(momentum2).mul(lr * (Math.sqrt(1.0 - beta) / (1.0 - alpha)));
    momentum2.powerTo(2.0);
    biasDelta = biasMomentum2 == 0.0 ? 0.0 : lr * biasMomentum / Math.sqrt(biasMomentum2);
  }
}
