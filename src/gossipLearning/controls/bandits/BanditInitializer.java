package gossipLearning.controls.bandits;

import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.utils.InstanceHolder;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class BanditInitializer implements Control {
  private static final String PAR_PROTE = "extractionProtocol";
  protected final int pidE;
  
  public BanditInitializer(String prefix) {
    pidE = Configuration.getPid(prefix + "." + PAR_PROTE);
    Machine.getInstance(prefix);
  }

  @Override
  public boolean execute() {
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      Protocol protocol = node.getProtocol(pidE);
      if (protocol instanceof ExtractionProtocol) {
        ExtractionProtocol extractionProtocol = (ExtractionProtocol) protocol;
        InstanceHolder instances = new InstanceHolder(0, 0);
        instances.add(null, 0.0);
        
        // set the instances for current node
        extractionProtocol.setInstanceHolder(instances);
      } else {
        throw new RuntimeException("The protocol " + pidE + " has to implement the ExtractionProtocol interface!");
      }
    }
    return false;
  }

}
