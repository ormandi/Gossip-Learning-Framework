package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.interfaces.models.Mergeable;
import peersim.core.Network;

public class UCBMerge extends UCB implements Mergeable<UCBMerge> {
  private static final long serialVersionUID = 2840791660561009595L;
  
  public UCBMerge(String prefix) {
    super(prefix);
  }
  
  protected UCBMerge(UCBMerge a){
    super(a);
  }
  
  public Object clone() {
    return new UCBMerge(this);
  }
  
  public void update() {
  }

  @Override
  public UCBMerge merge(UCBMerge model) {
    if (model.sumPlays == 0.0) {
      model.initArms();
    }
    // play the best arm and merge models
    int I = bestArmIdx();
    double xi = Machine.getInstance().play(I);
    double N = Network.size();
    double alpha = 1.0 - (1.0 / N);
    for (int i = 0; i < K; i++) {
      if (i == I) {
        rewards[i] += model.rewards[i] + xi;
        plays[i] += model.plays[i] + 1;
        sumPlays += model.plays[i] + 1;
        
        model.rewards[i] += xi;
        model.sumPlays += (model.plays[i] + 1) * alpha - model.plays[i];
        model.plays[i] = (model.plays[i] + 1) * alpha;
      } else {
        rewards[i] += model.rewards[i];
        plays[i] += model.plays[i];
        sumPlays += model.plays[i];
        
        model.sumPlays += model.plays[i] * alpha - model.plays[i];
        model.plays[i] *= alpha;
      }
    }
    return model;
  }

}
