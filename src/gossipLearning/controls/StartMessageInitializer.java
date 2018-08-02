package gossipLearning.controls;

import gossipLearning.messages.ActiveThreadMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * This class initializes the first wake-up messages for nodes in order to
 * the protocols could start.
 * 
 * @author István Hegedűs
 */
public class StartMessageInitializer implements Control {
  private static final String PAR_PROT = "protocols";
  private final int[] pid;
  private static final String PAR_DELAY = "delay";
  private final int delay;
  
  public StartMessageInitializer(String prefix) {
    String[] protNames = Configuration.getString(prefix + "." + PAR_PROT).split(",");
    pid = new int[protNames.length];
    for (int i = 0; i < protNames.length; i++) {
      pid[i] = Configuration.lookupPid(protNames[i]);
    }
    delay = Configuration.getInt(prefix + "." + PAR_DELAY, 0);
  }
  
  public boolean execute() {
    for (int index = 0; index < pid.length; index++) {
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        // schedule starter alarm
        EDSimulator.add(delay == 0 ? 0 : CommonState.r.nextInt(delay), ActiveThreadMessage.getInstance(), node, pid[index]);
      }
    }
    return false;
  }
}
