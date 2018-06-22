package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.P2Pegasos;
import gossipLearning.utils.VectorEntry;

/**
 * A mergeable version of the Pegasos algorithm.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeablePegasos.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable, Partializable {
  private static final long serialVersionUID = 5703095161342004957L;
  
  public MergeablePegasos(String prefix){
    super(prefix);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected MergeablePegasos(MergeablePegasos a){
    super(a);
  }
  
  public Object clone(){
    return new MergeablePegasos(this);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public Model merge(Model model) {
    MergeablePegasos m = (MergeablePegasos)model;
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
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    MergeablePegasos m = (MergeablePegasos)model;
    age += m.age * times;
    w.add(m.w, times);
    return this;
  }

  @Override
  public MergeablePegasos getModelPart() {
    return new MergeablePegasos(this);
  }

}
