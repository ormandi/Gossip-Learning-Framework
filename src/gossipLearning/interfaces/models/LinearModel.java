package gossipLearning.interfaces.models;

import gossipLearning.interfaces.optimizers.GD;
import gossipLearning.interfaces.optimizers.Optimizer;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public abstract class LinearModel extends ProbabilityModel implements SimilarityComputable<LinearModel> {
  private static final long serialVersionUID = -5680177111664068910L;
  private static final String PAR_OPIMIZER = "optimizer";
  
  protected SparseVector w;
  protected double bias;
  protected SparseVector gradient;
  protected double biasGradient;
  
  protected final Optimizer optimizer;
  
  public LinearModel(double lambda) {
    super(lambda);
    w = new SparseVector();
    bias = 0.0;
    gradient = new SparseVector();
    biasGradient = 0.0;
    optimizer = new GD();
  }
  
  public LinearModel(String prefix) {
    super(prefix);
    w = new SparseVector();
    bias = 0.0;
    gradient = new SparseVector();
    biasGradient = 0.0;
    String optimizerClass = Configuration.getString(prefix + "." + PAR_OPIMIZER);
    try {
      optimizer = (Optimizer)Class.forName(optimizerClass).getConstructor(String.class).newInstance(prefix + "." + PAR_OPIMIZER);
    } catch (Exception e) {
      throw new RuntimeException("Exception while creating optimizer: ", e);
    }
  }
  
  public LinearModel(LinearModel a) {
    super(a);
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    gradient = (SparseVector)a.gradient.clone();
    biasGradient = a.biasGradient;
    optimizer = (Optimizer)a.optimizer.clone();
  }
  
  protected abstract void gradient(SparseVector instance, double label);
  
  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double lr = eta / (isTime == 1 ? age : 1.0);
    
    gradient(instance, label);
    optimizer.delta(lr, gradient, biasGradient);
    
    w.add(optimizer.delta, -1.0);
    bias -= optimizer.biasDelta;
  }
  
  protected abstract void gradient(InstanceHolder instances);
  
  @Override
  public void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
    double lr = eta / (isTime == 1 ? age : 1.0);
    
    gradient(instances);
    optimizer.delta(lr, gradient, biasGradient);
    
    w.add(optimizer.delta, -1.0);
    bias -= optimizer.biasDelta;
  }
  
  @Override
  public void clear() {
    super.clear();
    w.clear();
    bias = 0.0;
    gradient.clear();
    biasGradient = 0.0;
  }
  
  @Override
  public double computeSimilarity(LinearModel model) {
    return w.cosSim(model.w);
  }
  
  @Override
  public final String toString() {
    return super.toString() + ", b: " + bias + ", w: " + w;
  }
  
  public Model merge(Model model) {
    LinearModel m = (LinearModel)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    w.mul(1.0 - modelWeight).add(m.w, modelWeight);
    bias += (m.bias - bias) * modelWeight;
    optimizer.merge(m.optimizer, modelWeight);
    return this;
  }
  
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  public Model add(Model model, double times) {
    LinearModel m = (LinearModel)model;
    age += m.age * times;
    w.add(m.w, times);
    bias += m.bias * times;
    optimizer.add(m.optimizer, times);
    return this;
  }

}
