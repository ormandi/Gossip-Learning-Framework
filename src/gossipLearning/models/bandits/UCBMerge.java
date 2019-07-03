package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import peersim.core.Network;

public class UCBMerge extends UCB implements Mergeable {
  private static final long serialVersionUID = 2840791660561009595L;
  
  public UCBMerge(String prefix) {
    super(prefix);
  }
  
  protected UCBMerge(UCBMerge a){
    super(a);
  }
  
  public UCBMerge clone() {
    return new UCBMerge(this);
  }
  
  public void update() {
  }

  @Override
  public Model merge(Model model) {
    UCBMerge m = (UCBMerge)model;
    if (m.sumPlays == 0.0) {
      m.initArms();
    }
    // play the best arm and merge models
    int I = bestArmIdx();
    double xi = Machine.getInstance().play(I);
    double N = Network.size();
    double alpha = 1.0 - (1.0 / N);
    for (int i = 0; i < K; i++) {
      if (i == I) {
        rewards[i] += m.rewards[i] + xi;
        plays[i] += m.plays[i] + 1;
        sumPlays += m.plays[i] + 1;
        
        m.rewards[i] += xi;
        m.sumPlays += (m.plays[i] + 1) * alpha - m.plays[i];
        m.plays[i] = (m.plays[i] + 1) * alpha;
      } else {
        rewards[i] += m.rewards[i];
        plays[i] += m.plays[i];
        sumPlays += m.plays[i];
        
        m.sumPlays += m.plays[i] * alpha - m.plays[i];
        m.plays[i] *= alpha;
      }
    }
    return model;
  }
  
  /*@Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }*/

}
