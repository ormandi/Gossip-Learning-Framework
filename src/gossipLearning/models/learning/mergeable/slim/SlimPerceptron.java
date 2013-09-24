package gossipLearning.models.learning.mergeable.slim;

import java.util.Arrays;
import java.util.Set;

import peersim.config.Configuration;
import gossipLearning.interfaces.functions.Binary;
import gossipLearning.interfaces.functions.ConstantGradient;
import gossipLearning.models.learning.mergeable.MergeablePerceptron;
import gossipLearning.utils.SparseVector;

public class SlimPerceptron extends MergeablePerceptron {
  private static final long serialVersionUID = -7462501717472741554L;
  protected static final String PAR_LAMBDA = "SlimPerceptron.lambda";
  protected static final String PAR_FUNC = "SlimPerceptron.usingSigmoid";
  
  public SlimPerceptron() {
    super();
  }
  
  public SlimPerceptron(SlimPerceptron a) {
    super(a);
  }
  
  protected SlimPerceptron (double age, double lambda, boolean usingSigmoid, int numberOfClasses, 
      double[] distribution, SparseVector w, double bias) {
    super(age, lambda, usingSigmoid, numberOfClasses, distribution, w, bias);
  }
  
  @Override
  public Object clone() {
    return new SlimPerceptron(this);
  }
  
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    usingSigmoid = Configuration.getBoolean(prefix + "." + PAR_FUNC);
    if (!usingSigmoid) {
      fAct = new Binary();
      fGrad = new ConstantGradient();
    }
  }
  
  @Override
  public SlimPerceptron merge(MergeablePerceptron a) {
    super.merge(a);
    return this;
  }
  
  @Override
  public SlimPerceptron getModelPart(Set<Integer> indices) {
    SparseVector w = new SparseVector(indices.size());
    for (int index : indices) {
      w.add(index, this.w.get(index));
    }
    return new SlimPerceptron(age, lambda, usingSigmoid, numberOfClasses, Arrays.copyOf(distribution, distribution.length), w, bias);
  }

}
