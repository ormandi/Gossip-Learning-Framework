package gossipLearning.controls.observers;

import gossipLearning.models.bandits.GlobalArmModel;
import gossipLearning.protocols.SimpleBanditProtocol2SentModels;

import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;

public class BanditPrecisionObserver extends BanditRegretObserver {
  public BanditPrecisionObserver(String prefix) throws Exception {
    super(prefix);
  }
  
  public boolean execute() {
    updateGraph();
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tavgavgE\tdevavgE\tmaxAvgE\tminAvgE" + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# "));
    }
    
    double max = 0.0;
    int maxIdx = 0;
    for (int j = 0; j < GlobalArmModel.numberOfArms(); j ++) {
      if (GlobalArmModel.getHiddenParameter(j) > max) {
        max = GlobalArmModel.getHiddenParameter(j);
        maxIdx = j;
      }
    }
    
    double avgError = 0.0;
    double devError = 0.0;
    double minError = 1.0;
    double maxError = 0.0;
    
    Set<Integer> idxSet = generateIndices();
    
    for (int i : idxSet) {
      Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof SimpleBanditProtocol2SentModels) {
        // evaluating best arm hit precision of the ith node
        SimpleBanditProtocol2SentModels protocol = (SimpleBanditProtocol2SentModels) p;
        
        double err = protocol.getSumHits() == 0.0 ? 0.0 : protocol.getArmHits()[maxIdx] / protocol.getSumHits();
        avgError += err;
        devError += err * err;
        if (err < minError) {
          minError = err;
        }
        if (err > maxError) {
          maxError = err;
        }
      }
    }
    avgError /= idxSet.size();
    devError /= idxSet.size();
    devError = Math.sqrt(devError - (avgError * avgError));
    if (CommonState.getTime() > 0) {
      if (format.equals("gpt")) {
        //System.out.println(CommonState.getTime() + "\t" + Configuration.getLong("simulation.logtime"));
        System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + avgError + "\t" + devError + "\t" + maxError + "\t" + minError + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# "));
      } else {
        System.out.println(getClass().getCanonicalName() + ":\tAvgE=" + avgError + "\tDevE=" + devError + "\tMaxE=" + maxError + "\tMinE=" + minError + ((printSuffix.length() > 0) ? "\t# " + printSuffix : "") );
      }
    }
    return false;
  }

}
