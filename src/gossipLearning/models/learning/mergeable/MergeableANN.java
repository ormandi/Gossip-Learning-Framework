package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.ANN;

public class MergeableANN extends ANN implements Mergeable, Partializable {
  private static final long serialVersionUID = 2342217682704545173L;
  public MergeableANN(String prefix) {
    super(prefix);
  }
  public MergeableANN(MergeableANN a) {
    super(a);
  }
  public Object clone() {
    return new MergeableANN(this);
  }
  @Override
  public Model getModelPart() {
    return new MergeableANN(this);
  }
  @Override
  public Model merge(Model model) {
    MergeableANN m = (MergeableANN)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    for (int i = 0; i < thetas.length; i++) {
      thetas[i].mulEquals(age / sum).addEquals(m.thetas[i], modelWeight);
    }
    age = Math.max(age, m.age);
    return this;
  }
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  @Override
  public Model add(Model model, double times) {
    MergeableANN m = (MergeableANN)model;
    age += m.age * times;
    for (int i = 0; i < thetas.length; i++) {
      thetas[i].addEquals(m.thetas[i], times);
    }
    return this;
  }

}
