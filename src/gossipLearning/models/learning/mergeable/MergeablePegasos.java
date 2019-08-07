package gossipLearning.models.learning.mergeable;

import java.util.Random;

import peersim.core.CommonState;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.P2Pegasos;

/**
 * A mergeable version of the Pegasos algorithm.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeablePegasos.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable, Partializable, Addable {
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
  
  public MergeablePegasos clone(){
    return new MergeablePegasos(this);
  }
  
  @Override
  public Model getModelPart() {
    return getModelPart(CommonState.r);
  }
  
  @Override
  public Model getModelPart(Random r) {
    return new MergeablePegasos(this);
  }

}
