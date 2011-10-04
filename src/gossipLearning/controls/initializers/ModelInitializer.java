package gossipLearning.controls.initializers;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class ModelInitializer implements Control {
  private static final String PAR_PROT = "protocol";
  private final int pid;
  
  public ModelInitializer(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }
  
  public boolean execute() {
    // init the nodes
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      Protocol protocol = node.getProtocol(pid);
      if (protocol instanceof ModelHolder) {
        ModelHolder<M> modelHolder = (ModelHolder<M>) protocol;
        modelHolder.initModel();
      }
    }
    return false;
  }


}
