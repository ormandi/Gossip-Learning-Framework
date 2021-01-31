package gossipLearning.controls;

import gossipLearning.interfaces.protocols.AbstractProtocol;
import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import peersim.config.Configuration;
import peersim.core.*;

public class LinearPredictionObserver implements Control {
  
  private static final String PAR_PROT = "protocol";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;
  /** @hidden*/
  protected boolean isPrintPrefix = true;
  
  protected final long logTime;
  
  public LinearPredictionObserver(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    logTime = new Scheduler(prefix,false).step;
  }
  
  @Override
  public boolean execute() {
    execute(false); //to suppress legacy eval
    for (int j = 0; j < Network.size(); j++) {
      Node node = Network.get(j);
      if (AbstractProtocol.nodeIsOnline(node,pid))
        ((LearningProtocol)node.getProtocol(pid)).forceEvaluate(pid);
    }
    execute(true);
    return false;
  }
  
  private void execute(boolean print) {
    LearningProtocol p = (LearningProtocol)Network.get(0).getProtocol(pid);
    for (AggregationResult result : p.getResults()) {
      if (isPrintPrefix) {
        System.out.println("#iter\t" + result.getNames());
        isPrintPrefix = false;
      }
      String res = CommonState.getTime()/logTime + "\t" + result;
      if (print)
        System.out.println(res);
    }
  }

}
