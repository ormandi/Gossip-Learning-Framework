package gossipLearning.controls;

import gossipLearning.overlays.TMan;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class TManInit implements Control {
  private static final String PAR_PROT = "protocol";
  
  protected final int pid;
  
  public TManInit(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }

  @Override
  public boolean execute() {
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      SparseVector descriptor = new SparseVector(new double[]{node.getID() + 1});
      ((TMan)node.getProtocol(pid)).initializeDescriptor(node, descriptor);
    }
    return false;
  }

}
