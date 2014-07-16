package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;

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
  private static final String PAR_LAMBDA = "MergeableLogReg.lambda";

  public MergeableLogReg(String prefix){
    super(prefix, PAR_LAMBDA);
  }
  
  protected MergeableLogReg(String prefix, String PAR_LAMBDA) {
    super(prefix, PAR_LAMBDA);
  }
  
  protected MergeableLogReg(MergeableLogReg a){
    super(a);
  }
  
  protected MergeableLogReg(double lambda, SparseVector w, double bias, double[] distribution, double age, int numberOfClasses) {
    super(lambda, w, bias, distribution, age, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeableLogReg(this);
  }
  
  @Override
  public MergeableLogReg merge(final MergeableLogReg model) {
    //age = Math.max(age, model.age);
    //bias = (bias + model.bias) * 0.5;
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
  public MergeableLogReg getModelPart(Set<Integer> indices) {
    return new MergeableLogReg(this);
  }
}
