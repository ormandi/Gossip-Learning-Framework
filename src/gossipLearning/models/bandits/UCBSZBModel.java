package gossipLearning.models.bandits;

import gossipLearning.interfaces.Mergeable;

import java.util.Map;

import peersim.core.Network;
import cern.colt.Arrays;

public class UCBSZBModel extends UCBModel implements Mergeable<UCBSZBModel> {
  private static final long serialVersionUID = -6397039807834688992L;
  
  protected double[] n_new;
  protected double sumN_new;
  
  public UCBSZBModel() {
    super();
    n_new = null;
    sumN_new = 0.0;
  }
  
  protected UCBSZBModel(UCBSZBModel a) {
    super(a.age, a.avgs, a.n, a.sumN);
    if (a.n_new != null) {
      n_new = a.n_new.clone();
    }
    sumN_new = a.sumN_new;
  }
  
  public Object clone() {
    return new UCBSZBModel(this);
  }
  
  public void init(String prefix) {
    super.init(prefix);
    n_new = n.clone();
    sumN_new = sumN;
  }
  
  @Override
  public UCBSZBModel merge(UCBSZBModel model) {
    // find best arm
    double ln = Math.sqrt(2.0*Math.log(sumN_new));
    int max = -1;
    double maxV = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < avgs.length; i ++) {
      double p = avgs[i] + ln/Math.sqrt(n_new[i]);
      if (p > maxV) {
        max = i;
        maxV = p;
      }
    }
    // play the best arm and merge models
    double x_t = GlobalArmModel.playMachine(max);
    double N = Network.size();
    double alpha = 1.0 - (1.0/N);
    for (int i = 0; i < avgs.length; i++) {
      if (i == max) {
        avgs[i] = (avgs[i]*n_new[i] + model.avgs[i]*model.n_new[i] + x_t) / (n_new[i] + model.n_new[i] + 1);
        n_new[i] += model.n_new[i] + 1;
        sumN_new += model.n_new[i] + 1;
        n[i] ++;
        sumN ++;
        
        model.avgs[i] = (model.avgs[i]*model.n_new[i] + x_t) / (model.n_new[i] + 1);
        model.sumN_new += (model.n_new[i] + 1)*alpha - model.n_new[i];
        model.n_new[i] = (model.n_new[i] + 1)*alpha;
        model.n[i] ++;
        model.sumN ++;
      } else {
        avgs[i] = (avgs[i]*n_new[i] + model.avgs[i]*model.n_new[i]) / (n_new[i] + model.n_new[i]);
        n_new[i] += model.n_new[i];
        sumN_new += model.n_new[i];
        
        model.sumN_new += model.n_new[i]*alpha - model.n_new[i];
        model.n_new[i] *= alpha;
      }
    }
    return model;
  }
  
  public void update(Map<Integer, Double> instance, double label) {
  }

  public String toString() {
    return "avg:" + Arrays.toString(avgs) + "\tn:" + Arrays.toString(n) + "\tsum:" + sumN + "\tn2:" + Arrays.toString(n_new) + "\tsum2:" + sumN_new;
  }
  
}
