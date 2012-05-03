package gossipLearning.models.multiClassLearners;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.SparseVector;

public class MergeableMultiLogReg extends MultiLogReg implements Mergeable<MergeableMultiLogReg> {
  private static final long serialVersionUID = -7800995106591726828L;

  public MergeableMultiLogReg() {
    super();
  }
  
  public MergeableMultiLogReg(MergeableMultiLogReg a) {
    lambda = a.lambda;
    age = a.age;
    numberOfClasses = a.numberOfClasses;
    if (a.w == null) {
      w = null;
      bias = null;
    } else {
      w = new SparseVector[numberOfClasses];
      for (int i = 0; i < numberOfClasses; i++) {
        w[i] = (SparseVector)a.w[i].clone();
      }
      bias = a.bias.clone();
    }
  }
  
  public Object clone() {
    return new MergeableMultiLogReg(this);
  }
  
  @Override
  public MergeableMultiLogReg merge(MergeableMultiLogReg model) {
    MergeableMultiLogReg result = new MergeableMultiLogReg(this);
    for (int i = 0; i < numberOfClasses; i++) {
      result.w[i].mul(0.5);
      result.w[i].add(model.w[i], 0.5);
      result.bias[i] = (bias[i] + model.bias[i]) / 2.0;
    }
    return result;
  }

}
