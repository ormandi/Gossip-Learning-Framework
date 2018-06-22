package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.Perceptron;
import gossipLearning.utils.VectorEntry;

public class MergeablePerceptron extends Perceptron implements Mergeable, Partializable {
  private static final long serialVersionUID = -2338065907068327013L;
  
  public MergeablePerceptron(String prefix) {
    super(prefix);
  }
  
  protected MergeablePerceptron(MergeablePerceptron a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new MergeablePerceptron(this);
  }
  
  @Override
  public Model merge(Model model) {
    MergeablePerceptron m = (MergeablePerceptron)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (VectorEntry e : m.w) {
      double value = w.get(e.index);
      //w.add(e.index, (e.value - value) * modelWeight);
      w.add(e.index, (e.value - value) * (value == 0 ? 1.0 : modelWeight));
    }
    bias += (m.bias - bias) * modelWeight;
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeablePerceptron m = (MergeablePerceptron)model;
    age += m.age * times;
    w.add(m.w, times);
    bias += m.bias * times;
    return this;
  }

  @Override
  public MergeablePerceptron getModelPart() {
    return new MergeablePerceptron(this);
  }

}
