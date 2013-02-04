package gossipLearning.controls;

import gossipLearning.overlays.TMan;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class TManTest implements Control {
  private static final String PAR_PROT = "protocol";
  
  protected final int pid;
  
  public TManTest(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }

  @Override
  public boolean execute() {
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      System.out.print(node.getID() + ":");
      TMan protocol = (TMan)node.getProtocol(pid);
      for (int j = 0; j < protocol.degree(); j++) {
        System.out.print(" " + protocol.getDescriptor(j));
      }
      System.out.println();
    }
    return false;
  }
}
