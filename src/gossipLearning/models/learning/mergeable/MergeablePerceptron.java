package gossipLearning.models.learning.mergeable;

import java.util.Random;

import peersim.core.CommonState;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.Perceptron;

public class MergeablePerceptron extends Perceptron implements Mergeable, Partializable, Addable {
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
  
  @Override
  public Model getModelPart() {
    return getModelPart(CommonState.r);
  }
  
  @Override
  public Model getModelPart(Random r) {
    return new MergeablePerceptron(this);
  }

}
