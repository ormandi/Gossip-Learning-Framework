package gossipLearning.interfaces.models;

import peersim.config.Configuration;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

public abstract class MomentumModel extends LinearModel {
  private static final long serialVersionUID = -834141430910150974L;
  private static final String PAR_ALPHA = "alpha";
  
  protected final double alpha;
  protected SparseVector momentun;
  protected double biasMomentum;
  
  public MomentumModel(double lambda) {
    super(lambda);
    alpha = 0.9;
    momentun = new SparseVector();
    biasMomentum = 0.0;
  }
  
  public MomentumModel(String prefix) {
    super(prefix);
    alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA, 0.9);
    momentun = new SparseVector();
    biasMomentum = 0.0;
  }
  
  public MomentumModel(MomentumModel a) {
    super(a);
    alpha = a.alpha;
    momentun = (SparseVector)a.momentun.clone();
    biasMomentum = a.biasMomentum;
  }
  
  public void update(SparseVector instance, double label) {
    age ++;
    double lr = eta / (isTime == 1 ? age : 1.0);
    
    gradient(instance, label);
    momentun.mul(alpha).add(gradient, lr);
    biasMomentum = (biasMomentum * alpha) + (lr * biasGradient);
    
    w.add(momentun, -1.0);
    bias -= biasMomentum;
  }
  
  public void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
    double lr = eta / (isTime == 1 ? age : 1.0);
    
    gradient(instances);
    momentun.mul(alpha).add(gradient, lr);
    biasMomentum = (biasMomentum * alpha) + (lr * biasGradient);
    
    w.add(momentun, -1.0);
    bias -= biasMomentum;
  }
  
  public void clear() {
    super.clear();
    momentun.clear();
    biasMomentum = 0.0;
  }
  
  public Model merge(Model model) {
    MomentumModel m = (MomentumModel)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    momentun.mul(1.0 - modelWeight).add(m.momentun, modelWeight);
    biasMomentum += (m.biasMomentum - biasMomentum) * modelWeight;
    super.merge(model);
    return this;
  }
  
  public Model add(Model model, double times) {
    MomentumModel m = (MomentumModel)model;
    momentun.add(m.momentun, times);
    biasMomentum += m.biasMomentum * times;
    super.add(model, times);
    return this;
  }

}
