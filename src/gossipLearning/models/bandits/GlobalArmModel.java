package gossipLearning.models.bandits;

import java.util.Random;

public class GlobalArmModel {
  private static Random rand;
  private static double[] armParams;
  
  public static void initialize(String prefix) {
    if (rand == null || armParams == null) {
      // TODO: fill from configuration
      rand = new Random(125689014);
      armParams = new double[]{0.9, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8};
    }   
  }
  
  public static double playMachine(int idx) {
    if (rand != null && armParams != null && 0 <= idx && idx < armParams.length) {
      return (rand.nextDouble() <= armParams[idx]) ? 1.0 : 0.0;
    }
    return Double.NaN;
  }
  
  public static int numberOfArms() {
    return (armParams != null) ? armParams.length : 0;
  }
  
  public static double getHiddenParameter(int idx) {
    if (armParams != null && 0 <= idx && idx < armParams.length) {
      return armParams[idx];
    }
    return Double.NaN;
  }

}
