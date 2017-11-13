package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.P2Pegasos;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;

/**
 * A mergeable version of the Pegasos algorithm.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>MergeablePegasos.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable<MergeablePegasos>, Partializable<MergeablePegasos> {
  private static final long serialVersionUID = 5703095161342004957L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "MergeablePegasos.lambda";
  
  public MergeablePegasos(String prefix){
    super(prefix, PAR_LAMBDA);
  }
  
  public MergeablePegasos(String prefix, String PAR_LAMBDA) {
    super(prefix, PAR_LAMBDA);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected MergeablePegasos(MergeablePegasos a){
    super(a);
  }
  
  protected MergeablePegasos(SparseVector w, double age, double[] distribution, double lambda, int numberOfClasses) {
    super(w, age, distribution, lambda, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeablePegasos(this);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public MergeablePegasos merge(MergeablePegasos model) {
    double sum = age + model.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = model.age / sum;
    age = Math.max(age, model.age);
    for (VectorEntry e : model.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * modelWeight);
    }
    return this;
  }

  @Override
  public MergeablePegasos getModelPart(Set<Integer> indices) {
    return new MergeablePegasos(this);
  }

}
