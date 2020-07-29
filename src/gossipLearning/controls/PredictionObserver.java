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
  
  protected final long logTime;
  
  public PredictionObserver(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    logTime = Configuration.getLong("simulation.logtime");
  }
  
  public boolean execute() {
    updateGraph();
    Protocol p = ((Node) g.getNode(0)).getProtocol(pid);
    if (p instanceof LearningProtocol) {
      for (AggregationResult result : ((LearningProtocol) p).getResults()) {
        if (isPrintPrefix) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println((CommonState.getTime()/logTime) + "\t" + result);
      }
    }
    /*Protocol p = null;
    for (int i = 0; i < g.size(); i++) {
      p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof LearningProtocol) {
        LearningProtocol prot = (LearningProtocol)p;
        Model[] models = prot.getModels();
        for (int j = 0; j < models.length; j++) {
          prot.getResultAggregator().push(pid, j, (LearningModel)models[j]);
        }
      }
    }
    
    if (p instanceof LearningProtocol) {
      for (AggregationResult result : ((LearningProtocol) p).getResults()) {
        if (isPrintPrefix) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println((CommonState.getTime()/logTime) + "\t" + result);
      }
    }*/
    isPrintPrefix = false;
    return false;
  }

}
