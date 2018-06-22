package gossipLearning.models.bandits;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.Utils;

import java.util.Arrays;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;

public class EGreedyMerge extends BanditModel implements Mergeable {
  private static final long serialVersionUID = -7737703030675783241L;
  private static final String PAR_C = "EGreedyMerge.c";
  private static final String PAR_D = "EGreedyMerge.d";
  
  // counters
  protected double[] n;
  protected double sumN = 0.0;
  
  // model
  protected double[] s;
  protected double[] w;
  protected double[] r;
  protected double[] q;
  protected double[] f;
  protected double[] g;
  
  // simulation related values
  public double d;
  public double c;
  
  public EGreedyMerge(String prefix) {
    super(prefix);
    c = Configuration.getDouble(prefix + "." + PAR_C);
    d = Configuration.getDouble(prefix + "." + PAR_D);
    
    // initialize counters
    n = new double[K];
    Arrays.fill(n, 0.0);
    sumN = 0.0;
    
    // model
    s = Arrays.copyOf(n, n.length);
    w = Arrays.copyOf(n, n.length);
    r = Arrays.copyOf(n, n.length);
    q = Arrays.copyOf(n, n.length);
    f = Arrays.copyOf(n, n.length);
    g = Arrays.copyOf(n, n.length);
    
    bestArmIndices = new Vector<Integer>();
  }
  
  protected EGreedyMerge(EGreedyMerge a) {
    super(a);
    
    // counters
    n = Arrays.copyOf(a.n, a.n.length);
    sumN = a.sumN;
    
    // model
    s = Arrays.copyOf(a.s, a.s.length);
    w = Arrays.copyOf(a.w, a.w.length);
    r = Arrays.copyOf(a.r, a.r.length);
    q = Arrays.copyOf(a.q, a.q.length);
    f = Arrays.copyOf(a.f, a.f.length);
    g = Arrays.copyOf(a.g, a.g.length);
    
    // parameter
    c = a.c;
    d = a.d;
    
    bestArmIndices = new Vector<Integer>();
  }

  @Override
  public Object clone() {
    return new EGreedyMerge(this);
  }
  
  public void update() {
    
    final double N = (double) Network.size();
    //final double m = Math.log(N)/Math.log(2.0);
    final double t = age;
    
    //final double eps = c*K/(d*d*t*N*N);
    final double eps = Math.min(1.0, c*K/(d*d*t*N));
    int I = 0; // index of the arm which will be played in the current run
    
    if (t == 1) {
      I = CommonState.r.nextInt(K);
    } else {
      final double r = CommonState.r.nextDouble();
      if (r < eps) {
        // random
        I = CommonState.r.nextInt(K);
      } else {
        // best
        I = bestArmIdx();
      }
      
    }
    
    // play arm I
    final double xi = Machine.getInstance().play(I);
    
    // update
    if (Utils.isPower2(t)) {
      addAndSetTo0(s, r);
      addAndSetTo0(w, q);
      addAndSetTo0(r, f);
      addAndSetTo0(q, g);
    }
    final double mul = N/2.0;
    f[I] += mul * xi;
    g[I] += mul;
    
    // update counters
    n[I]++;
    sumN ++;
  }
  
  protected static void addAndSetTo0(double[] first, double[] second) {
    final boolean init = first != null && second != null && first.length == second.length;
    for (int i = 0; init && i < first.length && i < second.length; i ++) {
      first[i] += second[i];
      second[i] = 0.0;
    }
  }
  
  protected Vector<Integer> bestArmIndices;
  protected int bestArmIdx() {
    //int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < s.length; i ++) {
      double v = w[i] == 0.0 ? 0.0 : s[i] / w[i];;
      if (v > maxV) {
        //max = i;
        maxV = v;
        bestArmIndices.clear();
        bestArmIndices.add(i);
      } else if (v == maxV) {
        bestArmIndices.add(i);
      }
    }
    //return max;
    return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
  }

  @Override
  public Model merge(Model model) {
    EGreedyMerge m = (EGreedyMerge)model;
    addAndSetTo0(s, m.s);
    addAndSetTo0(w, m.w);
    addAndSetTo0(r, m.r);
    addAndSetTo0(q, m.q);
    addAndSetTo0(f, m.f);
    addAndSetTo0(g, m.g);
    for (int i = 0; i < s.length; i ++) {
      s[i] /= 2.0;
      w[i] /= 2.0;
      r[i] /= 2.0;
      q[i] /= 2.0;
      f[i] /= 2.0;
      g[i] /= 2.0;
    }
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }

}