package gossipLearning.controls.bandits;

import java.util.Random;

/**
 * This class represents an arm of a multi-armed bandit machine.
 * @author István Hegedűs
 */
public class Arm {
  private double mu;
  private double plays;
  private double rewards;
  
  /**
   * Constructs an arm with the specified expected reward.
   * @param mu expected reward
   */
  public Arm(double mu) {
    this.mu = mu;
  }
  
  /**
   * Plays the arm and returns the reward, based on the specified random 
   * number generator and the value of the expected reward. </br>
   * reward = r.nextDouble() < mu ? 1.0 : 0.0
   * @param r random generator
   * @return reward
   */
  public double play(Random r) {
    double reward = r.nextDouble() < mu ? 1.0 : 0.0;
    plays ++;
    rewards += reward;
    return reward;
  }
  
  /**
   * Returns the expected reward.
   * @return the expected reward
   */
  public double getMu() {
    return mu;
  }
  
  /**
   * Returns the number of plays of this arm.
   * @return the number of plays
   */
  public double numPlays() {
    return plays;
  }
  
  /**
   * Returns the sum of rewards of this arm.
   * @return the sum of rewards
   */
  public double rewards() {
    return rewards;
  }
  
  public String toString() {
    return "" + mu;
  }
}
