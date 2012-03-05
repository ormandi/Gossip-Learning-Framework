package gossipLearning.models.bandits;

import java.util.Arrays;
import java.util.Map;

public class UCBModel implements BanditModel {
private static final long serialVersionUID = 5232458167435240109L;

  /** @hidden */
  protected double[] avgs;
  protected long[] n;
  protected long sumN;
  
  protected int age;
  
  public UCBModel(){
    avgs = null;
    n = null;
    age = 0;
  }
  
  protected UCBModel(int age, double[] avgs, long[] n, long sumN){
    this.age = age;
    this.avgs = (avgs == null) ? null : new double[avgs.length];
    this.n = (n == null) ? null : new long[n.length];
    for (int i = 0; i < avgs.length; i ++) {
      this.avgs[i] = avgs[i];
      this.n[i] = n[i];
    }
    this.sumN = sumN;
  }
  
  public Object clone(){
    return new UCBModel(age, avgs, n, sumN);
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    GlobalArmModel.initialize(prefix);
    
    avgs = new double[GlobalArmModel.numberOfArms()];
    n = new long[GlobalArmModel.numberOfArms()];
    
    // play each machine ones
    sumN = n.length;
    Arrays.fill(n, 1);
    for (int i = 0; i < avgs.length; i ++) {
      avgs[i] = GlobalArmModel.playMachine(i);
    }
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    if (sumN >= Double.MAX_VALUE) {
      throw new RuntimeException("Cannot convert to double, since " + sumN + " is greater than " + Double.MAX_VALUE);
    }
    // find best arm
    double ln = Math.sqrt(2.0*Math.log(sumN));
    int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < avgs.length; i ++) {
      double p = avgs[i] + ln/Math.sqrt((double)n[i]);
      if (p > maxV) {
        max = i;
        maxV = p;
      }
    }
    
    // play best arm
    double nu = 1.0 / (1.0 + n[max]);
    avgs[max] = (1.0 - nu) * avgs[max] + nu * GlobalArmModel.playMachine(max);
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
    if (0 <= armIdx && armIdx < avgs.length) {
      return avgs[armIdx];
    }
    return Double.NaN;
  }
  
  @Override
  public long numberOfPlayes(int armIdx) {
    if (0 <= armIdx && armIdx < n.length) {
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
