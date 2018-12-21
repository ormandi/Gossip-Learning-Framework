package gossipLearning.interfaces.optimizers;

import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class Adam extends Momentum {
  private static final long serialVersionUID = -4874780111488235358L;
  private static final String PAR_BETA = "beta";
  protected static final double EPS = 1E-8;
  
  protected final double beta;
  protected SparseVector momentum2;
  protected double biasMomentum2;
  
  protected double alphat;
  protected double betat;
  
  public Adam() {
    super();
    beta = 0.999;
    momentum2 = new SparseVector();
    biasMomentum2 = 0.0;
    alphat = 1.0;
    betat = 1.0;
  }
  
  public Adam(String prefix) {
    super(prefix);
    beta = Configuration.getDouble(prefix + "." + PAR_BETA);
    momentum2 = new SparseVector();
    biasMomentum2 = 0.0;
    alphat = 1.0;
    betat = 1.0;
  }
  
  public Adam(Adam a) {
    super(a);
    beta = a.beta;
    momentum2 = (SparseVector)a.momentum2.clone();
    biasMomentum2 = a.biasMomentum2;
    alphat = a.alphat;
    betat = a.betat;
  }
  
  @Override
  public Object clone() {
    return new Adam(this);
  }
  
  @Override
  public void delta(double lr, SparseVector gradient, double biasGradient) {
    alphat *= alpha;
    betat *= beta;
    
    momentum.mul(alpha).add(gradient, 1.0 - alpha);
    biasMomentum = (biasMomentum * alpha) + ((1.0 - alpha) * biasGradient);
    
    momentum2.mul(beta).add(gradient.powerTo(2.0), 1.0 - beta);
    //gradient.sqrt();
    biasMomentum2 = (biasMomentum2 * alpha) + ((1.0 - alpha) * biasGradient * biasGradient);
    
    momentum2.sqrt();
    // TODO: add EPS to momentum2
    delta.set(momentum).div(momentum2).mul(lr * (Math.sqrt(1.0 - betat) / (1.0 - alphat)));
    momentum2.powerTo(2.0);
    biasDelta = biasMomentum2 == 0.0 ? 0.0 : lr * (Math.sqrt(1.0 - betat) / (1.0 - alphat)) * biasMomentum / (Math.sqrt(biasMomentum2) + EPS);
  }
  
  @Override
  public Optimizer merge(Optimizer o, double weight) {
    // TODO: alphat, betat
    super.merge(o, weight);
    Adam m = (Adam)o;
    momentum2.mul(1.0 - weight).add(m.momentum2, weight);
    biasMomentum2 += (m.biasMomentum2 - biasMomentum2) * weight;
    return this;
  }
  
  @Override
  public Optimizer add(Optimizer o, double times) {
    // TODO: alphat, betat
    super.add(o, times);
    Adam m = (Adam)o;
    momentum2.add(m.momentum2, times);
    biasMomentum2 += m.biasMomentum2 * times;
    return this;
  }
}
