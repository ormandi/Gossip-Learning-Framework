package gossipLearning.controls.bandits;

import java.util.Random;

import peersim.config.Configuration;

public class Machine {
  private static final String PAR_SEED = "Bandits.seed";
  private static final String PAR_K = "Bandits.K";
  private static final String PAR_ARMS = "Bandits.arms";
  
  private Arm arms[];
  private Random r;
  private int K;
  
  private static Machine instance = null;
  
  private int bestArm;
  
  private Machine(long seed, int K) {
    r = new Random(seed);
    this.K = K;
    bestArm = K-1;
    int numOfArms = K;
    double slices = numOfArms -1;
    
    arms = new Arm[numOfArms];
    int c = 0;
    for (double i = 0.0; i <= slices; i++) {
      arms[c] = new Arm((0.8 / slices) * i + 0.1);
      c++;
    }
  }
  
  private Machine(String prefix) {
    long seed = Configuration.getLong(prefix + "." + PAR_SEED);
    r = new Random(seed);
    K = Configuration.getInt(prefix + "." + PAR_K);
    if (!Configuration.contains(prefix + "." + PAR_ARMS)) {
      bestArm = K-1;
      int numOfArms = K;
      double slices = numOfArms -1;
      
      arms = new Arm[numOfArms];
      int c = 0;
      for (double i = 0.0; i <= slices; i++) {
        arms[c] = new Arm((0.8 / slices) * i + 0.1);
        c++;
      }
    } else {
      String[] armParams = Configuration.getString(prefix + "." + PAR_ARMS).split(",");
      bestArm = -1;
      double bestParam = 0.0;
      arms = new Arm[armParams.length];
      for (int i = 0; i < arms.length; i++) {
        arms[i] = new Arm(Double.parseDouble(armParams[i]));
        if (arms[i].getParam() > bestParam) {
          bestParam = arms[i].getParam();
          bestArm = i;
        }
      }
    }
    //System.out.println("ARMS: " + Arrays.toString(arms));
    //d = (0.8 / (K-1)) * 0.9;
  }
  
  public double play(int index) {
    return arms[index].play(r);
  }
  
  public Arm getArm(int index) {
    return arms[index];
  }
  
  public int size() {
    return arms.length;
  }
  
  public double getPrecision() {
    double sum = 0.0;
    for (int i = 0; i < arms.length; i++) {
      sum += arms[i].numPlays();
    }
    return arms[bestArm].numPlays() / sum;
  }
  
  public double getRewards() {
    double sum = 0.0;
    for (int i = 0; i < arms.length; i++) {
      sum += arms[i].getParam() * arms[i].numPlays();
    }
    return sum;
  }
  
  public double getMaximalReward() {
    double sum = 0.0;
    for (int i = 0; i < arms.length; i++) {
      sum += arms[i].numPlays();
    }
    return arms[bestArm].getParam() * sum;
  }
  
  public double getRegret() {
    return getMaximalReward() - getRewards();
  }
  
  public static Machine getInstance(String prefix) {
    if (instance == null) {
      instance = new Machine(prefix);
    }
    
    return instance;
  }
  
  public static Machine getInstance(long seed, int K) {
    if (instance == null) {
      instance = new Machine(seed, K);
    }
    
    return instance;
  }
  
  public static Machine getInstance() {
    return instance;
  }
  
}
