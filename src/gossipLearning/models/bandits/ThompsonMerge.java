package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.Utils;
import peersim.core.CommonState;

public class ThompsonMerge extends Thompson implements Mergeable {
  private static final long serialVersionUID = -6683877819716369178L;

  public ThompsonMerge(String prefix) {
    super(prefix);
  }
  
  protected ThompsonMerge(ThompsonMerge a) {
    super(a);
  }

  @Override
  public ThompsonMerge clone() {
    return new ThompsonMerge(this);
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
  
  @Override
  public Model merge(Model model) {
    ThompsonMerge m = (ThompsonMerge)model;
    if (age == 0) return this;
    for (int i = 0; i < K; i++) {
      //plays[i] = (plays[i] + model.plays[i]) / (1.0);// - 1.0/age);
      //rewards[i] = (rewards[i] + model.rewards[i]) / (1.0);// - 1.0/age);
      plays[i] = (plays[i] + m.plays[i]) * 0.5;
      rewards[i] = (rewards[i] + m.rewards[i]) * 0.5;
    }
    return this;
  }
  
  /*@Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    ThompsonMerge m = (ThompsonMerge)model;
    for (int i = 0; i < K; i++) {
      plays[i] += m.plays[i] * times;
      rewards[i] += m.rewards[i] * times;
    }
    return this;
  }*/

}
