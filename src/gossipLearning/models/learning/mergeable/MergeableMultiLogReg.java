package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.MultiLogReg;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Arrays;
import java.util.Set;

import peersim.config.Configuration;

/**
 * This class represents a multi-class logistic regression classifier 
 * that can be merged to an other mergeable multi-class logistic regression classifier.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeableMultiLogReg.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeableMultiLogReg extends MultiLogReg implements Mergeable<MergeableMultiLogReg>, Partializable<MergeableMultiLogReg> {
  private static final long serialVersionUID = -7800995106591726828L;

  /** @hidden */
  protected static final String PAR_LAMBDA = "MergeableMultiLogReg.lambda";
  
  /**
   * Default constructor that calls the super();
   */
  public MergeableMultiLogReg() {
    super();
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  public MergeableMultiLogReg(MergeableMultiLogReg a) {
    super(a);
  }
  
  protected MergeableMultiLogReg(double lambda, double age, int numberOfClasses, 
      SparseVector[] w, double[] distribution, double[] v, double[] bias) {
    super(lambda, age, numberOfClasses, w, distribution, v, bias);
  }
  
  public Object clone() {
    return new MergeableMultiLogReg(this);
  }
  
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  @Override
  public MergeableMultiLogReg merge(MergeableMultiLogReg model) {
    //age = Math.max(age, model.age);
    for (int i = 0; i < numberOfClasses; i++) {
      //w[i].mul(0.5);
      //w[i].add(model.w[i], 0.5);
      //bias[i] = (bias[i] + model.bias[i]) * 0.5;
      for (VectorEntry e : model.w[i]) {
        double value = w[i].get(e.index);
        w[i].add(e.index, (e.value - value) * 0.5);
        bias[i] = (bias[i] + model.bias[i]) * 0.5;
      }
    }
    return this;
  }

  @Override
  public MergeableMultiLogReg getModelPart(Set<Integer> indices) {
    SparseVector[] w = new SparseVector[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      w[i] = new SparseVector(indices.size());
    }
    for (int index : indices) {
      for (int i = 0; i < numberOfClasses; i++) {
        w[i].add(index, this.w[i].get(index));
      }
    }
    return new MergeableMultiLogReg(lambda, age, numberOfClasses, w, 
        Arrays.copyOf(distribution, distribution.length), 
        Arrays.copyOf(v, v.length), 
        Arrays.copyOf(bias, bias.length));
  }

}
