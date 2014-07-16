package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.Function;
import gossipLearning.models.learning.mergeable.MergeablePerceptron;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Set;

public class SlimPerceptron extends MergeablePerceptron {
  private static final long serialVersionUID = -7462501717472741554L;
  protected static final String PAR_LAMBDA = "SlimPerceptron.lambda";
  protected static final String PAR_AFUNC = "SlimPerceptron.activation";
  protected static final String PAR_GFUNC = "SlimPerceptron.gradient";
  
  public SlimPerceptron(String prefix) {
    super(prefix, PAR_LAMBDA, PAR_AFUNC, PAR_GFUNC);
  }
  
  public SlimPerceptron(SlimPerceptron a) {
    super(a);
  }
  
  protected SlimPerceptron (double age, double lambda, Function fAct, Function fGrad, int numberOfClasses, double[] distribution, SparseVector w, double bias) {
    super(age, lambda, fAct, fGrad, numberOfClasses, distribution, w, bias);
  }
  
  @Override
  public Object clone() {
    return new SlimPerceptron(this);
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
    return new SlimPerceptron(age, lambda, fAct, fGrad, numberOfClasses, Arrays.copyOf(distribution, distribution.length), w, bias);
  }

}
