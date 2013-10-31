package gossipLearning.models.learning.mergeable;

import java.util.Set;

import peersim.config.Configuration;

import gossipLearning.interfaces.functions.Step;
import gossipLearning.interfaces.functions.ConstantGradient;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.Perceptron;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class MergeablePerceptron extends Perceptron implements Mergeable<MergeablePerceptron>, Partializable<MergeablePerceptron> {
  private static final long serialVersionUID = -2338065907068327013L;
  protected static final String PAR_LAMBDA = "MergeablePerceptron.lambda";
  protected static final String PAR_FUNC = "MergeablePerceptron.usingSigmoid";
  
  public MergeablePerceptron() {
    super();
  }
  
  protected MergeablePerceptron(MergeablePerceptron a) {
    super(a);
  }
  
  protected MergeablePerceptron (double age, double lambda, boolean usingSigmoid, int numberOfClasses, 
      double[] distribution, SparseVector w, double bias) {
    super(age, lambda, usingSigmoid, numberOfClasses, distribution, w, bias);
  }
  
  @Override
  public Object clone() {
    return new MergeablePerceptron(this);
  }
  
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    usingSigmoid = Configuration.getBoolean(prefix + "." + PAR_FUNC);
    if (!usingSigmoid) {
      fAct = new Step();
      fGrad = new ConstantGradient();
    }
  }

  @Override
  public MergeablePerceptron merge(MergeablePerceptron model) {
    //w.mul(0.5);
    //w.add(model.w, 0.5);
    for (VectorEntry e : model.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * 0.5);
    }
    bias = (bias + model.bias) * 0.5;
    return this;
  }

  @Override
  public MergeablePerceptron getModelPart(Set<Integer> indices) {
    return new MergeablePerceptron(this);
  }

}
