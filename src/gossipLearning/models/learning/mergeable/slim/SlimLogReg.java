package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.Set;

public class SlimLogReg extends MergeableLogReg {
  private static final long serialVersionUID = 6140967577949903596L;
  
  /** @hidden */
  private static final String PAR_LAMBDA = "SlimLogReg.lambda";

  public SlimLogReg(String prefix){
    super(prefix, PAR_LAMBDA);
  }
  
  protected SlimLogReg(SlimLogReg a){
    super(a);
  }
  
  protected SlimLogReg(double lambda, SparseVector w, double bias, double[] distribution, double age, int numberOfClasses) {
    super(lambda, w, bias, distribution, age, numberOfClasses);
  }
  
  public Object clone(){
    return new SlimLogReg(this);
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
