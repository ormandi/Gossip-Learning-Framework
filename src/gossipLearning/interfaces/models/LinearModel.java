package gossipLearning.interfaces.models;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

public abstract class LinearModel extends ProbabilityModel implements SimilarityComputable<LinearModel> {
  private static final long serialVersionUID = -5680177111664068910L;
  
  protected SparseVector w;
  protected double bias;
  protected SparseVector gradient;
  protected double biasGradient;
  
  public LinearModel(double lambda) {
    super(lambda);
    w = new SparseVector();
    bias = 0.0;
    gradient = new SparseVector();
    biasGradient = 0.0;
  }
  
  public LinearModel(String prefix) {
    super(prefix);
    w = new SparseVector();
    bias = 0.0;
    gradient = new SparseVector();
    biasGradient = 0.0;
  }
  
  public LinearModel(LinearModel a) {
    super(a);
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    gradient = (SparseVector)a.gradient.clone();
    biasGradient = a.biasGradient;
  }
  
  protected abstract void gradient(SparseVector instance, double label);

  @Override
  public final void update(SparseVector instance, double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    
    gradient(instance, label);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
  }
  
  protected abstract void gradient(InstanceHolder instances);

  @Override
  public final void update(InstanceHolder instances) {
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    
    gradient(instances);
    w.add(gradient, - nu);
    bias -= nu * biasGradient;
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
    w.mul(age / sum).add(m.w, modelWeight);
    bias += (m.bias - bias) * modelWeight;
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
    return this;
  }

}
