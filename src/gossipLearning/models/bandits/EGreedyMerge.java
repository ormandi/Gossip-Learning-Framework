package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

public class EGreedyMerge extends EGreedyModel implements Mergeable<EGreedyMerge> {
  private static final long serialVersionUID = -297797748721827962L;
  
  public EGreedyMerge() {
    super();
  }
  
  public EGreedyMerge(EGreedyMerge a) {
    super(a);
  }
  
  public Object clone() {
    return new EGreedyMerge(this);
  }

  @Override
  public EGreedyMerge merge(EGreedyMerge model) {
    for (int i = 0; i < K; i++) {
      rewards[i] += model.rewards[i];
      rewards[i] /= 2.0;
    }
    return this;
  }

}
