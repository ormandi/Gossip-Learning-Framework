package gossipLearning.models.learning;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.models.LinearModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class Perceptron extends LinearModel {
  private static final long serialVersionUID = -817025202609991782L;
  protected static final String PAR_AFUNC = "activation";
  protected static final String PAR_GFUNC = "gradient";
  
  protected final Function fAct;
  protected final Function fGrad;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public Perceptron(String prefix) {
    super(prefix);
    try {
      fAct = (Function)Class.forName(Configuration.getString(prefix + "." + PAR_AFUNC)).newInstance();
      fGrad = (Function)Class.forName(Configuration.getString(prefix + "." + PAR_GFUNC)).newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Can not create function. ", e);
    }
  }
  
  public Perceptron(Perceptron a) {
    super(a);
    fAct = a.fAct;
    fGrad = a.fGrad;
  }
  
  @Override
  public Object clone() {
    return new Perceptron(this);
  }

  @Override
  protected void gradient(SparseVector instance, double label) {
    double product = w.mul(instance) + bias;
    double grad = (fAct.execute(product) - label) * fGrad.execute(product);
    gradient.set(w).mul(lambda).add(instance, grad);
    biasGradient = grad;
  }
  
  @Override
  protected void gradient(InstanceHolder instances) {
    gradient.set(w).mul(lambda * instances.size());
    biasGradient = 0.0;
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      double product = w.mul(instance) + bias;
      double grad = (fAct.execute(product) - label) * fGrad.execute(product);
      gradient.add(instance, grad);
      biasGradient += grad;
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double activation = fAct.execute(w.mul(instance) + bias);
    distribution[0] = 1.0 - activation;
    distribution[1] = activation;
    return distribution;
  }

}
