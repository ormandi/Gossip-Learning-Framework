package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

public class UCBMergeModel extends UCBModel implements Mergeable<UCBMergeModel> {
  private static final long serialVersionUID = -6946599308445305782L;
  
  public UCBMergeModel() {
    super();
  }
  
  protected UCBMergeModel(UCBMergeModel a) {
    super(a.age, a.avgs, a.n, a.sumN);
  }
  
  public Object clone() {
    return new UCBMergeModel(this);
  }
  
  @Override
  public UCBMergeModel merge(UCBMergeModel model) {
    for (int i = 0; i < n.length; i++) {
      avgs[i] = (avgs[i] + model.avgs[i])/2.0;
    }
    return this;
  }

}
