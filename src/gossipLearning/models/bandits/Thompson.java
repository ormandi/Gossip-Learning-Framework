package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.utils.Utils;
import peersim.core.CommonState;

public class Thompson extends BanditModel {
  private static final long serialVersionUID = 4586264363022230495L;

  public Thompson(String prefix) {
    super(prefix);
  }
  
  protected Thompson(Thompson a){
    super(a);
  }
  
  @Override
  public Thompson clone() {
    return new Thompson(this);
  }
  
  @Override
  public void update() {
    int I = -1;
    double theta = 0.0;
    double max = 0.0;
    for (int i = 0; i < K; i++) {
      theta = Utils.nextBetaFast(rewards[i] + 1, plays[i] - rewards[i] + 1, CommonState.r);
      if (theta > max) {
        max = theta;
        I = i;
      }
    }
    double xi = Machine.getInstance().play(I);
    rewards[I] += xi;
    plays[I]++;
    sumPlays ++;
    sumRewards += xi;
  }

}
