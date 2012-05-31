package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.Utils;

import java.util.Arrays;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;

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
  protected int I;
  
  private static P2GreedySlim[] peerToModel = null;
  private static String prefix;
  
  // simulation related values
  public static double K;
  public static double d;
  public double c;
  
  public P2GreedySlim() {
  }
  
  protected P2GreedySlim(P2GreedySlim o) {
    // initialize peer to model mapping
    if (peerToModel == null) {
      //System.out.println("Initialize table, with prefix: " + prefix);
      peerToModel = new P2GreedySlim[Network.size()];
      for (int i = 0; i < peerToModel.length; i ++) {
        peerToModel[i] = new P2GreedySlim();
        peerToModel[i].init(prefix);
        peerToModel[i].I = i % GlobalArmModel.numberOfArms();
      }
      
      //for (int i = 0; i < peerToModel.length; i ++) {
      //  System.out.println(i + "\t" + peerToModel[i].I);
      //}
    }
    
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
    final double N = (double) Network.size();
    final double t = (double) ++age;
    P2GreedySlim M = getMyModel();
    
    final double eps = Math.min(1.0, c*K/(d*d*t*N));
    int Il = 0;  // index of the arm which will be played in the current run
    
    final double r = CommonState.r.nextDouble();
    if (r < eps) {
      // random
      Il = M.I;
    } else {
      // best
      Il = bestArmIdx();
    }    
    
    // play arm I
    final double xi = GlobalArmModel.playMachine(Il);
    
    // perform updates
    if (Il == I) {
      // the played arm is based on the received model
      update(xi, c, t);
      if (I == M.I) {
        peerToModel[I] = (P2GreedySlim) this.clone();
        M = getMyModel();
      } else {
        M.update(0.0, 0.0, t);
      }
    } else {
      // the played arm is based on the model stored at the current peer
      M.update(xi, c, t);
      update(0.0, 0.0, t);
    }
    
    // update counters
    n[I]++;
    sumN ++;
    
    return I;
  }
  
  private void update(double xi, double c, double t) {
    if (Utils.isPower2(t)) {
      s += r;
      w += q;
      r = f;
      q = g;
      f = 0.0;
      g = 0.0;
    }
    final double mul = ((double)Network.size())/2.0;
    f += c * mul * xi;
    g += c * mul;
  }
  
  @Override
  public double predict(int armIdx) {
    final P2GreedySlim m = getMyModel();
    if (m.I == armIdx && m.w != 0.0) {
      return m.s / m.w;
    } else if (I == armIdx && w != 0.0) {
      return s / w;
    }
    return 0.0;
  }
  
  public P2GreedySlim getMyModel() {
    return peerToModel[(int) CommonState.getNode().getID()];
  }
  
  public int getI() {
    return this.I;
  }
  
  private int bestArmIdx() {
    final P2GreedySlim m = getMyModel();
    return (this.predict(this.I) >= m.predict(m.I)) ? this.I : m.I;
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    P2GreedySlim.prefix = prefix;
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
    if (I == m.I) {
      s += m.s; s /= 2.0;
      w += m.w; w /= 2.0;
      r += m.r; r /= 2.0;
      q += m.q; q /= 2.0;
      f += m.f; f /= 2.0;
      g += m.g; g /= 2.0;
      return this;
    }
    return (this.predict(this.I) >= m.predict(m.I)) ? this : m;
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
