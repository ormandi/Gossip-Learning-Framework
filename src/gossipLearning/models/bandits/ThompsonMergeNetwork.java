package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class ThompsonMergeNetwork extends Thompson implements Mergeable {
  private static final long serialVersionUID = -2950995113604982921L;
  
  private static final String PAR_N = "ThompsonMergeNetwork.N";
  private final double N;

  public ThompsonMergeNetwork(String prefix) {
    super(prefix);
    N = Configuration.getDouble(prefix + "." + PAR_N);
  }
  
  protected ThompsonMergeNetwork(ThompsonMergeNetwork a) {
    super(a);
    N = a.N;
  }
  
  @Override
  public ThompsonMergeNetwork clone() {
    return new ThompsonMergeNetwork(this);
  }
  
  @Override
  public void update() {
    //double N = Network.size()*Math.log(Network.size());
    //double N = 1.0;
    //double N = Network.size();
    int I = -1;
    double theta = 0.0;
    double max = 0.0;
    for (int i = 0; i < K; i++) {
      theta = Utils.nextBetaFast(N * (rewards[i] + 1), N * (plays[i] - rewards[i] + 1), CommonState.r);
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
    ThompsonMergeNetwork m = (ThompsonMergeNetwork)model;
    for (int i = 0; i < K; i++) {
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
    ThompsonMergeNetwork m = (ThompsonMergeNetwork)model;
    for (int i = 0; i < K; i++) {
      plays[i] += m.plays[i] * times;
      rewards[i] += m.rewards[i] * times;
    }
    return this;
  }*/
  
}
