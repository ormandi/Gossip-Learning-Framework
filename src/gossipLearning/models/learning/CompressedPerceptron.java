package gossipLearning.models.learning;

import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class CompressedPerceptron extends Perceptron {
  private static final long serialVersionUID = -2727461616417241381L;
  private static final String PAR_NBITS = "nbits";
  
  protected final int nbits;
  
  public CompressedPerceptron(String prefix) {
    super(prefix);
    nbits = Configuration.getInt(prefix + "." + PAR_NBITS);
  }
  
  public CompressedPerceptron(CompressedPerceptron a) {
    super(a);
    nbits = a.nbits;
  }
  
  @Override
  public Object clone() {
    return new CompressedPerceptron(this);
  }
  
  @Override
  protected void gradient(SparseVector instance, double label) {
    double product = w.mul(instance) + bias;
    double grad = (fAct.execute(product) - label) * fGrad.execute(product);
    gradient.set(instance).mul(grad).scaleValueRange(nbits, CommonState.r);
    gradient.add(w, lambda);
    biasGradient = Utils.scaleValueRange(grad, nbits, CommonState.r) * lambda;
  }
  
  protected SparseVector inst_tmp = new SparseVector();
  @Override
  protected void gradient(InstanceHolder instances) {
    gradient.clear();
    biasGradient = 0.0;
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double product = w.mul(instance) + bias;
      double grad = (fAct.execute(product) - label) * fGrad.execute(product);
      inst_tmp.set(instance).mul(grad).scaleValueRange(nbits, CommonState.r);
      gradient.add(inst_tmp);
      biasGradient += Utils.scaleValueRange(grad, nbits, CommonState.r);
    }
    gradient.add(w, lambda * instances.size());
    biasGradient *= lambda;
  }

}
