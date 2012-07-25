package gossipLearning.models;

import peersim.config.Configuration;
import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.SparseVector;

/**
 * A mergeable version of the Pegasos algorithm.
 * @author István Hegedűs
 *
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable<MergeablePegasos> {
  private static final long serialVersionUID = 5703095161342004957L;
  
  protected static final String PAR_LAMBDA = "MergeablePegasos.lambda";
  
  public MergeablePegasos(){
    super();
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameters using the super constructor.
   * @param w hyperplane
   * @param age model age
   * @param lambda learning parameter
   */
  protected MergeablePegasos(SparseVector w, double age, double lambda, int numberOfClasses){
    super(w, age, lambda, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeablePegasos(w, age, lambda, numberOfClasses);
  }
  
  public void init(String prefix) {
    super.init(prefix);
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public MergeablePegasos merge(final MergeablePegasos model) {
    SparseVector mergedw = new SparseVector(w);
    double age = Math.max(this.age, model.age);
    
    mergedw.mul(0.5);
    mergedw.add(model.w, 0.5);
    
    return new MergeablePegasos(mergedw, age, lambda, numberOfClasses);
  }

}
