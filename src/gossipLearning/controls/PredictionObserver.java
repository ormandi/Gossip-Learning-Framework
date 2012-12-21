package gossipLearning.controls;

import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

/**
 * This class computes the prediction error of the nodes in the network. 
 * The computed prediction error will be written on the output channel.
 * 
 * @author István Hegedűs
 * 
 * @navassoc - - - LearningProtocol
 */
public class PredictionObserver extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;
  /** @hidden*/
  protected boolean isPrintPrefix = true;
  
  public PredictionObserver(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }
  
  public boolean execute() {
    updateGraph();
    Protocol p = ((Node) g.getNode(0)).getProtocol(pid);
    if (p instanceof LearningProtocol) {
      if (isPrintPrefix) {
        for (AggregationResult result : ((LearningProtocol) p).getResults()) {
          System.out.println("#iter\t" + result.getNames());
        }
        isPrintPrefix = false;
      }
      for (AggregationResult result : ((LearningProtocol) p).getResults()) {
        System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + result);
      }
    }
    return false;
  }

}
