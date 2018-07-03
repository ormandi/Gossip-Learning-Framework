package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.Perceptron;

public class MergeablePerceptron extends Perceptron implements Mergeable, Partializable {
  private static final long serialVersionUID = -2338065907068327013L;
  
  public MergeablePerceptron(String prefix) {
    super(prefix);
  }
  
  protected MergeablePerceptron(MergeablePerceptron a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new MergeablePerceptron(this);
  }
  
  @Override
  public MergeablePerceptron getModelPart() {
    return new MergeablePerceptron(this);
  }

}
