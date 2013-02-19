package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

public class UCBMerge extends UCB implements Mergeable<UCBMerge>{
  private static final long serialVersionUID = -6874693492447675608L;
  
  public UCBMerge() {
    super();
  }
  
  public UCBMerge(UCBMerge a) {
    super(a);
  }
  
  public Object clone() {
    return new UCBMerge(this);
  }

  @Override
  public UCBMerge merge(UCBMerge model) {
    for (int i = 0; i < K; i++) {
      rewards[i] += model.rewards[i];
      rewards[i] /= 2.0;
    }
    return this;
  }

}
