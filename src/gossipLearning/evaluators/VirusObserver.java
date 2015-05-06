package gossipLearning.evaluators;

import gossipLearning.models.Virus;
import gossipLearning.protocols.VirusProtocol;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.HashSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;


public class VirusObserver implements Control {
  private static final String PAR_PID = "protocol";
  protected final int pid;
  
  protected long numExecutes;
  
  protected HashSet<Long> set;
  
  public VirusObserver(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
    set = new HashSet<Long>();
    numExecutes = 0;
  }

  @Override
  public boolean execute() {
    if (numExecutes >= 1000) {
      double infected = 0.0;
      SparseVector vector = new SparseVector(Network.size());
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        Virus model = (Virus)((VirusProtocol)node.getProtocol(pid)).getModel();
        vector.add(model.getVector());
        if (node.getFailState() == Fallible.OK) {
          set.add(node.getID());
        }
        infected += model.isInfecter() ? 1.0 : 0.0;
      }
      vector.mul(1.0 / (Network.size() * (numExecutes - 1000)));
      System.out.println((numExecutes-1000) + "\t" + (infected / Network.size()) + "\t" + (set.size() / (double)Network.size()));
      if (CommonState.getTime() >= CommonState.getEndTime() - 1000) {
        System.out.println("Model: " + vector);
        for (VectorEntry e : vector) {
          System.out.println(e.index + "\t" + e.value);
        }
      }
    }
    numExecutes ++;
    return false;
  }

}
