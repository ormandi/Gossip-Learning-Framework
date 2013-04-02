package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Set;

import peersim.config.Configuration;

/**
 * This class represents the logistic regression classifier that 
 * can be merged to an other mergeable logistic regression classifier.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeableLogReg.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeableLogReg extends LogisticRegression implements Mergeable<MergeableLogReg>, Partializable<MergeableLogReg> {
  private static final long serialVersionUID = -4465428750554412761L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "MergeableLogReg.lambda";

  /**
   * Default constructor that calls the super();
   */
  public MergeableLogReg(){
    super();
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  protected MergeableLogReg(MergeableLogReg a){
    super(a);
  }
  
  protected MergeableLogReg(double lambda, SparseVector w, double bias, 
      double[] distribution, double age, int numberOfClasses) {
    super(lambda, w, bias, distribution, age, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeableLogReg(this);
  }
  
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  @Override
  public MergeableLogReg merge(final MergeableLogReg model) {
    age = Math.max(age, model.age);
    bias = (bias + model.bias) / 2.0;
    w.mul(0.5);
    w.add(model.w, 0.5);
    return this;
  }

  @Override
  public MergeableLogReg getModelPart(Set<Integer> indices) {
    SparseVector w = new SparseVector(indices.size());
    for (int index : indices) {
      w.add(index, this.w.get(index));
    }
    return new MergeableLogReg(lambda, w, bias, Arrays.copyOf(distribution, distribution.length), age, numberOfClasses);
  }
}
