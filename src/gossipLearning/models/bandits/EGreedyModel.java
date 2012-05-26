package gossipLearning.models.bandits;

import java.util.Arrays;

import peersim.core.CommonState;
import peersim.core.Network;

public class EGreedyModel extends AbstractBanditModel {
  private static final long serialVersionUID = -5717062818990132175L;

  protected double[] n;
  protected double[] rewards;
  protected double sum;
  protected long age;
  
  public static double K = (double) GlobalArmModel.numberOfArms();
  public static double d = GlobalArmModel.getDValue();
  public static double c = 0.2; 
  
  public EGreedyModel() {
  }
  
  protected EGreedyModel(EGreedyModel a) {
    n = a.n.clone();
    rewards = a.rewards.clone();
    sum = a.sum;
    age = a.age;
  }
  
  public Object clone() {
    return new EGreedyModel(this);
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    GlobalArmModel.initialize(prefix);
    
    //c = 1.0/GlobalArmModel.numberOfArms() * Network.size();
    K = (double) GlobalArmModel.numberOfArms();
    d = GlobalArmModel.getDValue();
    
    // initialize counters
    n = new double[GlobalArmModel.numberOfArms()];
    Arrays.fill(n, 0.0);
    rewards = new double[GlobalArmModel.numberOfArms()];
    Arrays.fill(rewards, 0.0);
    sum = 0.0;
    age = 0;
  }

  @Override
  public int update() {
    final double N = (double) Network.size();
    final double t = (double) ++age;
    
    final double eps = c*K/(d*d*t);
    int I = 0;  // index of the arm which will be played in the current run
    
    if (t == 1) {
      I = CommonState.r.nextInt(GlobalArmModel.numberOfArms());
    } else {
      final double r = CommonState.r.nextDouble();
      if (r < eps) {
        // random
        I = CommonState.r.nextInt(GlobalArmModel.numberOfArms());
      } else {
        // best
        I = bestArmIdx();
      }
      
    }
    
    // play arm I
    final double xi = GlobalArmModel.playMachine(I);
    rewards[I] += xi;
    n[I]++;
    sum ++;
    
    return I;
  }
  
  private int bestArmIdx() {
    int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < n.length; i ++) {
      double v = predict(i);
      if (v > maxV) {
        max = i;
        maxV = v;
      }
    }
    return max;
  }

  @Override
  public double predict(int armIdx) {
    return n[armIdx] == 0.0 ? 0.0 : rewards[armIdx] / n[armIdx];
  }

  @Override
  public double numberOfPlayes(int armIdx) {
    return n[armIdx];
  }

  @Override
  public double numberOfAllPlayes() {
    return sum;
  }

}
