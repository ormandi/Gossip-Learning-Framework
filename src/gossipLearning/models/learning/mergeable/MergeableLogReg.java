package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.LogisticRegression;
import gossipLearning.utils.VectorEntry;

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
public class MergeableLogReg extends LogisticRegression implements Mergeable, Partializable {
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
  
  public Object clone(){
    return new MergeableLogReg(this);
  }
  
  @Override
  public Model merge(Model model) {
    MergeableLogReg m = (MergeableLogReg)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (VectorEntry e : m.w) {
      double value = w.get(e.index);
      //w.add(e.index, (e.value - value) * modelWeight);
      w.add(e.index, (e.value - value) * (value == 0 ? 1.0 : modelWeight));
    }
    bias += (m.bias - bias) * modelWeight;
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeableLogReg m = (MergeableLogReg)model;
    age += m.age * times;
    w.add(m.w, times);
    bias += m.bias * times;
    return this;
  }

  @Override
  public Model getModelPart() {
    return new MergeableLogReg(this);
  }
  
}
