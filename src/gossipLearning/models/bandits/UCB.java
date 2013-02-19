package gossipLearning.models.bandits;

import peersim.core.CommonState;

public class UCB extends EGreedyModel {
  private static final long serialVersionUID = -6013343367480893665L;
  
  public UCB() {
    super();
  }
  
  public UCB(UCB a) {
    super(a);
  }
  
  public Object clone() {
    return new UCB(this);
  }
  
  @Override
  public int update() {
    int I = bestArmIdx();
    
    // play arm I
    final double xi = GlobalArmModel.playMachine(I);
    rewards[I] += xi;
    n[I]++;
    sum ++;
    
    return I;
  }
  
  @Override
  public double predict(int armIdx) {
    double result = n[armIdx] == 0.0 ? 1.0 : (rewards[armIdx] / n[armIdx]) + Math.sqrt(2.0 * Math.log(sum) / n[armIdx]);
    return result;
  }
  
  protected int bestArmIdx() {
    double maxV = Double.NEGATIVE_INFINITY;
    // find unplayed arms
    bestArmIndices.clear();
    for (int i = 0; i < n.length; i++) {
      if (n[i] == 0.0) {
        bestArmIndices.add(i);
      }
    }
    if (bestArmIndices.size() > 0) {
      return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
    }
    // find best arm
    for (int i = 0; i < n.length; i ++) {
      double v = predict(i);
      if (v > maxV) {
        maxV = v;
        bestArmIndices.clear();
        bestArmIndices.add(i);
      } else if (v == maxV) {
        bestArmIndices.add(i);
      }
    }
    return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
  }

}
