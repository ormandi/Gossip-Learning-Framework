package gossipLearning.models.bandits;

import gossipLearning.interfaces.Model;

import java.util.Arrays;
import java.util.Map;

import peersim.config.Configuration;

public class UCBModel implements Model {
private static final long serialVersionUID = 5232458167435240109L;

  protected static final String PAR_ARMS = "arms";
  protected int arms = 1;
  
  /** @hidden */
  protected double[] sums;
  protected int[] n;
  
  protected int age;
  
  public UCBModel(){
    sums = null;
    n = null;
    age = 0;
  }
  
  protected UCBModel(int arms, int age, double[] sums, int[] n){
    
  }
  
  public Object clone(){
    return new UCBModel(arms, age, sums, n);
  }
  
  @Override
  public void init(String prefix) {
    // init global arm model
    GlobalArmModel.initialize(prefix);
    
    arms = Configuration.getInt(prefix + "." + PAR_ARMS, 1);
    sums = new double[arms];
    n = new int[arms];
    
    // play each machine ones
    Arrays.fill(n, 1);
    for (int i = 0; i < sums.length; i ++) {
      sums[i] = GlobalArmModel.playMachine(i);
    }
  }

  @Override
  public void update(Map<Integer, Double> instance, double label) {
    // TODO Auto-generated method stub

  }

  @Override
  public double predict(Map<Integer, Double> instance) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getNumberOfClasses() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    // TODO Auto-generated method stub

  }

}
