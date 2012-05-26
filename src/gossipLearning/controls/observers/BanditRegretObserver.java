package gossipLearning.controls.observers;

import gossipLearning.models.bandits.GlobalArmModel;
import gossipLearning.protocols.SimpleBanditProtocol2SentModels;

import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

public class BanditRegretObserver extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  /**
   * The protocol ID.
   */
  protected final int pid;
  private static final String PAR_FORMAT = "format";
  /**
   * The type of print format.
   * @hidden
   */
  protected final String format;
  
  private static final String PAR_SUFFIX = "suffix";
  protected String printSuffix = "";
    
  public BanditRegretObserver(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
    printSuffix = Configuration.getString(prefix + "." + PAR_SUFFIX, "");
  }
  
  /**
   * Returns the set of the node indices in the graph.
   * @return set of node indices
   */
  protected Set<Integer> generateIndices() {
    TreeSet<Integer> indices = new TreeSet<Integer>();
    for (int i = 0; i < g.size(); i ++) {
      indices.add(i);
    }
    return indices;
  }
  
  public boolean execute() {
    updateGraph();
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tavgavgE\tdevavgE\tmaxAvgE\tminAvgE" + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# "));
    }
    
    double avgError = 0.0;
    double devError = 0.0;
    double minError = Double.POSITIVE_INFINITY;
    double maxError = 0.0;
    
    Set<Integer> idxSet = generateIndices();
    
    for (int i : idxSet) {
      Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof SimpleBanditProtocol2SentModels) {
        // evaluating the regret of the ith node
        SimpleBanditProtocol2SentModels protocol = (SimpleBanditProtocol2SentModels) p;
        double predictedValue = 0.0;
        double max = 0.0;
        int maxIdx = 0;
        for (int j = 0; j < GlobalArmModel.numberOfArms(); j ++) {
          if (GlobalArmModel.getHiddenParameter(j) > max) {
            max = GlobalArmModel.getHiddenParameter(j);
            maxIdx = j;
          }
          predictedValue += GlobalArmModel.getHiddenParameter(j) * protocol.getArmHits()[j];
        }
        double expectedValue = GlobalArmModel.getHiddenParameter(maxIdx) * protocol.getSumHits();
        double err = expectedValue - predictedValue;
        avgError += err;
        //devError += err * err;
        if (err < minError) {
          minError = err;
        }
        if (err > maxError) {
          maxError = err;
        }
      }
    }
    //avgError /= idxSet.size();
    //devError /= idxSet.size();
    //devError = Math.sqrt(devError - (avgError * avgError));
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

  /**
   * Returns the current value of print suffix which is appended to each result line.
   * By default it is an empty string
   * @return print suffix
   */
  public String getPrintSuffix() {
    return printSuffix;
  }
  
  /**
   * Sets the value of print suffix. This will be appended to each result line (after a '#').
   * @param printSuffix new value of result line suffix
   */
  public void setPrintSuffix(String printSuffix) {
    this.printSuffix = printSuffix;
  }
}
