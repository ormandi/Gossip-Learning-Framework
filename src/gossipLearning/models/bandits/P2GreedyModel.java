package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

import java.util.Arrays;

import peersim.core.Network;

public class P2GreedyModel extends AbstractBanditModel implements Mergeable<P2GreedyModel> {
  private static final long serialVersionUID = -7737703030675783241L;
  
  // counters
  protected double[] n;
  protected double sumN;
  
  // age
  protected int age;
  
  // model
  protected double[] s;
  protected double[] w;
  protected double[] r;
  protected double[] q;
  protected double[] f;
  protected double[] g;
  
  // simulation related values
  public static double N = (double) Network.size();
  public static double K = (double) GlobalArmModel.numberOfArms();
  public static double d = GlobalArmModel.getDValue();
  public static double m = Math.log(N)/Math.log(2.0);
  
  protected P2GreedyModel(int age, double[] n, double sumN, double[] s, double[] w, double[] r, double[] q, double[] f, double[] g){
    // age
    this.age = age;
    
    // counters
    this.n = Arrays.copyOf(n, n.length);
    this.sumN = sumN;
    
    // model
    this.s = Arrays.copyOf(s, s.length);
    this.w = Arrays.copyOf(w, w.length);
    this.r = Arrays.copyOf(r, r.length);
    this.q = Arrays.copyOf(q, q.length);
    this.f = Arrays.copyOf(f, f.length);
    this.g = Arrays.copyOf(g, g.length);
  }

  @Override
  public Object clone() {
    return new P2GreedyModel(age, n, sumN, s, w, r, q, f, g);
  }
  
  @Override
  public void init(String prefix) {
    // initialize global arm model
    GlobalArmModel.initialize(prefix);
    
    // initialize counters
    n = new double[GlobalArmModel.numberOfArms()];
    Arrays.fill(n, 0.0);
    sumN = 0.0;
    
    // model
    s = new double[GlobalArmModel.numberOfArms()];
    w = new double[GlobalArmModel.numberOfArms()];
    r = new double[GlobalArmModel.numberOfArms()];
    q = new double[GlobalArmModel.numberOfArms()];
    f = new double[GlobalArmModel.numberOfArms()];
    g = new double[GlobalArmModel.numberOfArms()];
  }
  
  public void update() {
    double t = (double) ++age;
    //double c = ;
    
    //double eps = 
    int I = 0;  // index of the arm which will be played in the current run
    
    // TODO: update
    
    // update counters
    n[I]++;
    sumN ++;
  }

  @Override
  public P2GreedyModel merge(P2GreedyModel model) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double predict(int armIdx) {
    if (0 <= armIdx && armIdx < n.length) {
      if (w[armIdx] != 0.0) {
        return s[armIdx] / w[armIdx];
      } else {
        return 0.0;
      }
    }
    return Double.NaN;
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
