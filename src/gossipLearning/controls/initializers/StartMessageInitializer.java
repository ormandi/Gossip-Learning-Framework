package gossipLearning.controls.initializers;

import gossipLearning.messages.ActiveThreadMessage;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * This class initializes the first wake up messages for nodes in order to
 * the learning could start. 
 * @author István Hegedűs
 *
 */
public class StartMessageInitializer implements Control {
  private static final String PAR_PROT = "protocol";
  private final int pid;
  private static final String PAR_DELAY = "delay";
  private final int delay;
  
  public StartMessageInitializer(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    delay = Configuration.getInt(prefix + "." + PAR_DELAY, 0);
  }
  
  public boolean execute() {
    // init desriptor lists
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      // schedule starter alarm
      EDSimulator.add(delay, ActiveThreadMessage.getInstance(), node, pid);
    }
    return false;
  }
}
