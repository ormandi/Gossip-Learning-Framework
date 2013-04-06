package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Set;

import peersim.config.Configuration;

public class SlimLogReg extends MergeableLogReg {
  private static final long serialVersionUID = 6140967577949903596L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "SlimLogReg.lambda";

  /**
   * Default constructor that calls the super();
   */
  public SlimLogReg(){
    super();
  }
  
  /**
   * Constructs an object by clones (deep copy) the specified object.
   * @param a to be cloned.
   */
  protected SlimLogReg(SlimLogReg a){
    super(a);
  }
  
  protected SlimLogReg(double lambda, SparseVector w, double bias, 
      double[] distribution, double age, int numberOfClasses) {
    super(lambda, w, bias, distribution, age, numberOfClasses);
  }
  
  public Object clone(){
    return new SlimLogReg(this);
  }
  
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
  }
  
  @Override
  public SlimLogReg merge(final MergeableLogReg model) {
    super.merge(model);
    return this;
  }

  @Override
  public SlimLogReg getModelPart(Set<Integer> indices) {
    SparseVector w = new SparseVector(indices.size());
    for (int index : indices) {
      w.add(index, this.w.get(index));
    }
    return new SlimLogReg(lambda, w, bias, Arrays.copyOf(distribution, distribution.length), age, numberOfClasses);
  }

}
