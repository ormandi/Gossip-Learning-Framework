package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;

import java.util.Vector;

import peersim.core.CommonState;

public class UCB extends BanditModel {
  private static final long serialVersionUID = -5105200915598899296L;
  
  protected Vector<Integer> bestArmIndices;
  
  public UCB(String prefix) {
    super(prefix);
    bestArmIndices = new Vector<Integer>();
  }
  
  protected UCB(UCB a) {
    super(a);
    bestArmIndices = new Vector<Integer>();
  }
  
  @Override
  public UCB clone() {
    return new UCB(this);
  }

  @Override
  public void update() {
    int I = bestArmIdx();
    double xi = Machine.getInstance().play(I);
    plays[I] ++;
    rewards[I] += xi;
    sumPlays ++;
    sumRewards += xi;
  }
  
  protected void initArms() {
    double xi;
    for (int i = 0; i < K; i++) {
      xi = Machine.getInstance().play(i);
      plays[i] ++;
      rewards[i] += xi;
      sumPlays ++;
      sumRewards += xi;
    }
  }
  
  protected int bestArmIdx() {
    if (sumPlays == 0.0) {
      initArms();
    }
    //int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    double numerator = Math.sqrt(2.0*Math.log(sumPlays));
    for (int i = 0; i < K; i ++) {
      double v = (rewards[i] / plays[i]) + (numerator / Math.sqrt(plays[i]));
      if (v > maxV) {
        //max = i;
        maxV = v;
        bestArmIndices.clear();
        bestArmIndices.add(i);
      } else if (v == maxV) {
        bestArmIndices.add(i);
      }
      //System.out.println(v + "\t" + plays[i] + "\t" + sumPlays);
    }
    //return max;
    return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
  }

}
