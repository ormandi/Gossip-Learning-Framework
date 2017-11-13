package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.Perceptron;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;

public class MergeablePerceptron extends Perceptron implements Mergeable<MergeablePerceptron>, Partializable<MergeablePerceptron> {
  private static final long serialVersionUID = -2338065907068327013L;
  protected static final String PAR_LAMBDA = "MergeablePerceptron.lambda";
  protected static final String PAR_AFUNC = "MergeablePerceptron.activation";
  protected static final String PAR_GFUNC = "MergeablePerceptron.gradient";
  
  
  public MergeablePerceptron(String prefix) {
    super(prefix, PAR_LAMBDA, PAR_AFUNC, PAR_GFUNC);
  }
  
  public MergeablePerceptron(String prefix, String PAR_LAMBDA, String PAR_AFUNC, String PAR_GFUNC) {
    super(prefix, PAR_LAMBDA, PAR_AFUNC, PAR_GFUNC);
  }
  
  protected MergeablePerceptron(MergeablePerceptron a) {
    super(a);
  }
  
  protected MergeablePerceptron (double age, double lambda, Function fAct, Function fGrad, int numberOfClasses, double[] distribution, SparseVector w, double bias) {
    super(age, lambda, fAct, fGrad, numberOfClasses, distribution, w, bias);
  }
  
  @Override
  public Object clone() {
    return new MergeablePerceptron(this);
  }
  
  @Override
  public MergeablePerceptron merge(MergeablePerceptron model) {
    double sum = age + model.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = model.age / sum;
    age = Math.max(age, model.age);
    for (VectorEntry e : model.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * modelWeight);
    }
    bias += (model.bias - bias) * modelWeight;
    return this;
  }

  @Override
  public MergeablePerceptron getModelPart(Set<Integer> indices) {
    return new MergeablePerceptron(this);
  }

}
