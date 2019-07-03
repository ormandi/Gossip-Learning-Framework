package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.LogisticRegression;

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
public class MergeableLogReg extends LogisticRegression implements Mergeable, Partializable, Addable {
  private static final long serialVersionUID = -4465428750554412761L;
  
  public MergeableLogReg(double lambda) {
    super(lambda);
  }
  
  public MergeableLogReg(String prefix){
    super(prefix);
  }
  
  protected MergeableLogReg(MergeableLogReg a){
    super(a);
  }
  
  public MergeableLogReg clone(){
    return new MergeableLogReg(this);
  }

  @Override
  public Model getModelPart() {
    return new MergeableLogReg(this);
  }
  
}
