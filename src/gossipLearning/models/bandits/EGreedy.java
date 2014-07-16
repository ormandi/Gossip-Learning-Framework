package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;

import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class EGreedy extends BanditModel {
  private static final long serialVersionUID = -4407620276397110332L;
  private static final String PAR_C = "EGreedy.c";
  private static final String PAR_D = "EGreedy.d";
  
  protected double c;
  protected double d;
  protected Vector<Integer> bestArmIndices;
  
  public EGreedy() {
    super();
    bestArmIndices = new Vector<Integer>();
  }
  
  public EGreedy(EGreedy a) {
    super(a);
    c = a.c;
    d = a.d;
    bestArmIndices = new Vector<Integer>();
  }
  
  public void init(String prefix) {
    super.init(prefix);
    c = Configuration.getDouble(prefix + "." + PAR_C);
    d = Configuration.getDouble(prefix + "." + PAR_D);
  }

  @Override
  public Object clone() {
    return new EGreedy(this);
  }

  @Override
  public void update() {
    final double t = (double) age;
    
    final double eps = c*K/(d*d*t);
    int I = 0; // index of the arm which will be played in the current run
    
    if (t == 1) {
      I = CommonState.r.nextInt(K);
    } else {
      final double r = CommonState.r.nextDouble();
      if (r < eps) {
        // random
        I = CommonState.r.nextInt(K);
      } else {
        // best
        I = bestArmIdx();
      }
      
    }
    
    // play arm I
    final double xi = Machine.getInstance().play(I);
    rewards[I] += xi;
    plays[I]++;
    sumPlays ++;
    sumRewards += xi;
  }
  
  protected int bestArmIdx() {
    //int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < plays.length; i ++) {
      double v = plays[i] == 0.0 ? 0.0 : rewards[i] / plays[i];
      if (v > maxV) {
        //max = i;
        maxV = v;
        bestArmIndices.clear();
        bestArmIndices.add(i);
      } else if (v == maxV) {
        bestArmIndices.add(i);
      }
    }
    //return max;
    return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
  }
  
}
