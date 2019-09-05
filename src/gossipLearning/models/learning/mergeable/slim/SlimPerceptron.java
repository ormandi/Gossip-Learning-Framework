package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeablePerceptron;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimPerceptron extends MergeablePerceptron implements SlimModel, Partializable {
  private static final long serialVersionUID = -7462501717472741554L;
  private static final String PAR_SIZE = "size";
  
  protected final int modelSize;
  
  public SlimPerceptron(String prefix) {
    super(prefix);
    modelSize = Configuration.getInt(prefix + "." + PAR_SIZE);
  }
  
  public SlimPerceptron(SlimPerceptron a) {
    super(a);
    modelSize = a.modelSize;
  }
  
  @Override
  public SlimPerceptron clone() {
    return new SlimPerceptron(this);
  }
  
  @Override
  public Model merge(Model model) {
    SlimPerceptron m = (SlimPerceptron)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (VectorEntry e : m.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * modelWeight);
    }
    bias += (m.bias - bias) * modelWeight;
    return this;
  }
  
  @Override
  public Model getModelPart(Random r) {
    SlimPerceptron result = new SlimPerceptron(this);
    result.w.clear();
    if (gradient.size() == 0) {
      return result;
    }
    double[] weights = new double[gradient.size()];
    for (int i = 0; i < gradient.size(); i++) {
      weights[i] = modelSize < 0 ? 1.0 : Math.abs(gradient.valueAt(i));
    }
    WeightedRandPerm rp = new WeightedRandPerm(r, weights);
    rp.reset(gradient.size());
    int iter = Math.abs(modelSize);
    while (0 < iter && rp.hasNext()) {
      iter --;
      int idx = gradient.indexAt(rp.next());
      result.w.add(idx, w.get(idx));
    }
    return result;
  }
  
  private double biasWeight = 0.0;
  private SparseVector weight;
  @Override
  public Model weightedAdd(Model model, double times) {
    if (weight == null) {
      weight = new SparseVector();
    } else {
      // if the w initialization is not 0 do not clean
      w.pointMul(weight);
      weight.mul(biasWeight);
    }
    super.add(model, times);
    SlimPerceptron m = (SlimPerceptron)model;
    biasWeight += times;
    for (VectorEntry entry : m.w) {
      weight.add(entry.index, times);
    }
    weight.mul(1.0 / biasWeight);
    w.div(weight);
    return this;
  }
  
  @Override
  public void clear() {
    super.clear();
    weight = null;
    biasWeight = 0.0;
  }

}
