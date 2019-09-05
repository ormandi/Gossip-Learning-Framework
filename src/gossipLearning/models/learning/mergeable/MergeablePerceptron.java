package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.Perceptron;

public class MergeablePerceptron extends Perceptron implements Mergeable {
  private static final long serialVersionUID = -2338065907068327013L;
  
  public MergeablePerceptron(String prefix) {
    super(prefix);
  }
  
  protected MergeablePerceptron(MergeablePerceptron a) {
    super(a);
  }
  
  @Override
  public MergeablePerceptron clone() {
    return new MergeablePerceptron(this);
  }

}
