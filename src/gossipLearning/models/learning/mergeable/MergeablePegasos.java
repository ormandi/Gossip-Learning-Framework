package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.P2Pegasos;
import peersim.config.Configuration;

/**
 * A mergeable version of the Pegasos algorithm.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeablePegasos.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable<MergeablePegasos> {
  private static final long serialVersionUID = 5703095161342004957L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "MergeablePegasos.lambda";
  
  public MergeablePegasos(){
    super();
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
  
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public MergeablePegasos merge(MergeablePegasos model) {
    age = Math.max(age, model.age);
    w.mul(0.5);
    w.add(model.w, 0.5);
    return this;
  }

}
