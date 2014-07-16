package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeablePegasos;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Set;

public class SlimPegasos extends MergeablePegasos {
  private static final long serialVersionUID = 6849809999453437967L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "SlimPegasos.lambda";
  
  public SlimPegasos(String prefix){
    super(prefix, PAR_LAMBDA);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected SlimPegasos(SlimPegasos a){
    super(a);
  }
  
  protected SlimPegasos(SparseVector w, double age, double[] distribution, 
      double lambda, int numberOfClasses) {
    super(w, age, distribution, lambda, numberOfClasses);
  }
  
  public Object clone(){
    return new SlimPegasos(this);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public SlimPegasos merge(MergeablePegasos model) {
    super.merge(model);
    return this;
  }

  @Override
  public SlimPegasos getModelPart(Set<Integer> indices) {
    SparseVector w = new SparseVector(indices.size());
    for (int index : indices) {
      w.add(index, this.w.get(index));
    }
    return new SlimPegasos(w, age, Arrays.copyOf(distribution, distribution.length), lambda, numberOfClasses);
  }

}
