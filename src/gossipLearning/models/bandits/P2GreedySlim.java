package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

import java.util.Arrays;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class P2GreedySlim extends AbstractBanditModel implements Mergeable<P2GreedySlim> {
  private static final long serialVersionUID = 8312708154569649141L;
  
  // counters
  protected double[] n;
  protected double sumN = 0.0;
  
  // age
  protected long age = 0;
  
  // model
  protected double s;
  protected double w;
  protected double r;
  protected double q;
  protected double f;
  protected double g;
  // index of arm
  protected int I;
  
  // simulation related values
  public static double K;
  public static double d;
  public double c;
  
  public P2GreedySlim() {
  }
  
  protected P2GreedySlim(P2GreedySlim o) {
    // age
    this.age = o.age;
    
    // counters
    this.n = Arrays.copyOf(o.n, o.n.length);
    this.sumN = o.sumN;
    
    // model
    this.s = o.s;
    this.w = o.w;
    this.r = o.r;
    this.q = o.q;
    this.f = o.f;
    this.g = o.g;
    this.I = o.I;
    
    // parameter
    this.c = o.c;
  }

  @Override
  public Object clone() {
    return new P2GreedySlim(this);
  }
  
  @Override
  public int update() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  @Override
  public double predict(int armIdx) {
    // TODO predict
    return 0.0;
    /*
    if (0 <= armIdx && armIdx < n.length) {
      if (w[armIdx] != 0.0) {
        return s[armIdx] / w[armIdx];
      } else {
        return 0.0;
      }
    }
    return Double.NaN;
    */
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    GlobalArmModel.initialize(prefix);
    
    // initialize static parameters
    K = (double) GlobalArmModel.numberOfArms();
    d = GlobalArmModel.getDValue();
    
    // initialize counters
    n = new double[GlobalArmModel.numberOfArms()];
    Arrays.fill(n, 0.0);
    sumN = 0.0;
    
    // model
    s = 0.0;
    w = 0.0;
    r = 0.0;
    q = 0.0;
    f = 0.0;
    g = 0.0;
    
    // set parameter I
    I = (int) CommonState.getNode().getID() % GlobalArmModel.numberOfArms();
    //System.out.println("Node: " + CommonState.getNode().getID() + ", Arm: " + I);
    
    
    // parameter
    c = Configuration.getDouble(prefix + ".p2greedy.C");
  }

  @Override
  public P2GreedySlim merge(P2GreedySlim m) {
    s += m.s; s /= 2.0;
    w += m.w; w /= 2.0;
    r += m.r; r /= 2.0;
    q += m.q; q /= 2.0;
    f += m.f; f /= 2.0;
    g += m.g; g /= 2.0;
    return this;
  }
  
  @Override
  public double numberOfPlayes(int armIdx) {
    if (0 <= armIdx && armIdx < n.length) {
      return n[armIdx];
    }
    return Double.NaN;
  }

  @Override
  public double numberOfAllPlayes() {
    return sumN;
  }
}
