package gossipLearning.controls.bandits;

import java.util.Random;

public class Arm {
  private double param;
  private double plays;
  private double rewards;
  
  public Arm(double param) {
    this.param = param;
  }
  
  public double play(Random r) {
    double reward = r.nextDouble() < param ? 1.0 : 0.0;
    plays ++;
    rewards += reward;
    return reward;
  }
  
  public double getParam() {
    return param;
  }
  
  public double numPlays() {
    return plays;
  }
  
  public double rewards() {
    return rewards;
  }
  
  public String toString() {
    return "" + param;
  }
}
