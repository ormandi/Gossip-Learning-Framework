package gossipLearning.controls;

import gossipLearning.overlays.TMan;
import peersim.config.Configuration;
import peersim.core.CommonState;
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
    double mean = 0.0;
    double dev = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      System.out.print(node.getID() + ":");
      TMan protocol = (TMan)node.getProtocol(pid);
      double sum = 0.0;
      for (int j = 0; j < protocol.degree(); j++) {
        System.out.print(" " + protocol.getDescriptor(j));
        sum += protocol.getDescriptor(j).getSimilarity();
      }
      mean += sum;
      dev += sum*sum;
      System.out.println();
    }
    mean /= Network.size();
    dev /= Network.size();
    dev -= mean * mean;
    dev = Math.sqrt(dev);
    System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + mean + "\t(" + dev + ")");
    return false;
  }
}
