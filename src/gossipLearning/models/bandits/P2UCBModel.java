package gossipLearning.models.bandits;

import gossipLearning.utils.Utils;
import peersim.core.CommonState;
import peersim.core.Network;

public class P2UCBModel extends P2GreedyModel {
  private static final long serialVersionUID = 2806376992204655971L;
  
  public P2UCBModel() {
    super();
  }
  
  public P2UCBModel(P2UCBModel a) {
    super(a.age, a.n, a.sumN, a.s, a.w, a.r, a.q, a.f, a.g, a.c);
  }
  
  public Object clone() {
    return new P2UCBModel(this);
  }
  
  public int update() {
    
    final double N = (double) Network.size();
    //final double m = Math.log(N)/Math.log(2.0);
    final double t = (double) ++age;
    
    int I = bestArmIdx();  // index of the arm which will be played in the current run
    
    // play arm I
    final double xi = GlobalArmModel.playMachine(I);
    
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
    
    return I;
  }
  
  public double predict(int armIdx) {
    return w[armIdx] == 0.0 ? 1.0 : (s[armIdx] / w[armIdx]) + Math.sqrt(2.0*Math.log(Network.size() * sumN)/w[armIdx]);
    /*if (0 <= armIdx && armIdx < n.length) {
      if (w[armIdx] != 0.0) {
        return (s[armIdx] / w[armIdx]) + Math.sqrt(2.0*Math.log(Network.size() * sumN)/w[armIdx]);
      } else {
        return 0.0;
      }
    }
    return Double.NaN;*/
  }
  
  protected int bestArmIdx() {
    double maxV = Double.NEGATIVE_INFINITY;
    // find unplayed arms
    bestArmIndices.clear();
    for (int i = 0; i < n.length; i++) {
      if (w[i] == 0.0) {
        bestArmIndices.add(i);
      }
    }
    if (bestArmIndices.size() > 0) {
      return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
    }
    // find best arm
    for (int i = 0; i < n.length; i ++) {
      double v = predict(i);
      if (v > maxV) {
        maxV = v;
        bestArmIndices.clear();
        bestArmIndices.add(i);
      } else if (v == maxV) {
        bestArmIndices.add(i);
      }
    }
    return bestArmIndices.get(CommonState.r.nextInt(bestArmIndices.size()));
  }

}
