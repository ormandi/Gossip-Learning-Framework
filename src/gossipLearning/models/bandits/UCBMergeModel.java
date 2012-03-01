package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

public class UCBMergeModel extends UCBModel implements Mergeable<UCBMergeModel> {
  private static final long serialVersionUID = -6946599308445305782L;

  @Override
  public UCBMergeModel merge(UCBMergeModel model) {
    for (int i = 0; i < n.length; i++) {
      sums[i] += model.sums[i];
      n[i] += model.n[i];
      sumN += model.sumN;
    }
    return this;
  }

}
