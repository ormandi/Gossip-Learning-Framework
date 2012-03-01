package gossipLearning.models.bandits;

import java.util.Arrays;
import java.util.Map;

import peersim.config.Configuration;

public class UCBModel implements BanditModel {
private static final long serialVersionUID = 5232458167435240109L;

  protected static final String PAR_ARMS = "arms";
  protected int arms = 1;
  
  /** @hidden */
  protected double[] sums;
  protected int[] n;
  protected long sumN;
  
  protected int age;
  
  public UCBModel(){
    sums = null;
    n = null;
    age = 0;
  }
  
  protected UCBModel(int arms, int age, double[] sums, int[] n, long sumN){
    this.arms = arms;
    this.age = age;
    this.sums = new double[arms];
    this.n = new int[arms];
    for (int i = 0; i < sums.length; i ++) {
      this.sums[i] = sums[i];
      this.n[i] = n[i];
    }
    this.sumN = sumN;
  }
  
  public Object clone(){
    return new UCBModel(arms, age, sums, n, sumN);
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    GlobalArmModel.initialize(prefix);
    
    arms = Configuration.getInt(prefix + "." + PAR_ARMS, 1);
    sums = new double[arms];
    n = new int[arms];
    
    // play each machine ones
    sumN = n.length;
    Arrays.fill(n, 1);
    for (int i = 0; i < sums.length; i ++) {
      sums[i] = GlobalArmModel.playMachine(i);
    }
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    // find best arm
    double ln = Math.sqrt(2.0*Math.log(sumN));
    int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < sums.length; i ++) {
      double p = sums[i]/(double)n[i] + ln/Math.sqrt((double)n[i]);
      if (p > maxV) {
        max = i;
        maxV = p;
      }
    }
    
    // play best arm
    sums[max] += GlobalArmModel.playMachine(max);
    n[max] ++;
    sumN ++;
  }

  @Override
  public double predict(Map<Integer, Double> instance) {
    if (instance != null && instance.size() == 1) {
      Integer armIdxO = instance.keySet().iterator().next();
      int armIdx = (armIdxO != null) ? armIdxO.intValue() : -1 ;
      return predict(armIdx);
    }
    return Double.NaN;
  }
  
  @Override
  public double predict(int armIdx) {
    if (0 <= armIdx && armIdx < sums.length) {
      return sums[armIdx]/(double)n[armIdx];
    }
    return Double.NaN;
  }
  
  @Override
  public int numberOfPlayes(int armIdx) {
    if (0 <= armIdx && armIdx < sums.length) {
      return n[armIdx];
    }
    return -1;
  }
  
  @Override
  public long numberOfAllPlayes() {
    return sumN;
  }

  @Override
  public int getNumberOfClasses() {
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
  }

}
