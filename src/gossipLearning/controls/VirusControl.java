package gossipLearning.controls;

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

public class VirusControl implements Control {
  private static final String PAR_PID = "protocol";
  protected final int pid;
  private static final String PAR_START = "startTime";
  protected final long startTime;
  private static final String PAR_RESET = "resetStep";
  protected final long reset;
  protected final long simulationLogTime;
  
  protected int superPeerIdx;
  protected HashSet<Long> set;
  
  public VirusControl(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
    startTime = Configuration.getLong(prefix + "." + PAR_START);
    reset = Configuration.getLong(prefix + "." + PAR_RESET);
    simulationLogTime = Configuration.getLong("simulation.logtime");
    set = new HashSet<Long>();
    superPeerIdx = -1;
  }
  
  @Override
  public boolean execute() {
    long currentTime = CommonState.getTime();
    // warm up
    if (currentTime < startTime) {
      return false;
    }
    
    // reset
    if ((currentTime - startTime) % reset == 0) {
      set.clear();
      if (superPeerIdx != -1) {
        ((VirusProtocol)Network.get(superPeerIdx).getProtocol(pid)).setSessionLength(0);
      
        SparseVector vector = new SparseVector(Network.size());
        double sumAge = 0.0;
        for (int i = 0; i < Network.size(); i++) {
          VirusProtocol protocol = (VirusProtocol)Network.get(i).getProtocol(pid);
          Virus model = (Virus)protocol.getModel();
          vector.add(model.getVector());
          sumAge += model.getAge();
          model.reset();
        }
        vector.mul(1.0 / sumAge);
        System.out.print("#" + ((currentTime - startTime) / reset));
        for (VectorEntry e : vector) {
          System.out.print(" " + e.index + " " + e.value);
        }
        System.out.println();
      }
      superPeerIdx = CommonState.r.nextInt(Network.size());
      Node node = Network.get(superPeerIdx);
      node.setFailState(Fallible.OK);
      ((VirusProtocol)node.getProtocol(pid)).initSession(node, pid);
      ((VirusProtocol)node.getProtocol(pid)).setSessionLength(CommonState.getEndTime());
      ((Virus)((VirusProtocol)node.getProtocol(pid)).getModel()).setInfected();
    }
    
    // evaluate
    double infected = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      Virus model = (Virus)((VirusProtocol)node.getProtocol(pid)).getModel();
      if (node.getFailState() == Fallible.OK) {
        set.add(node.getID());
      }
      infected += model.isInfecter() ? 1.0 : 0.0;
    }
    System.out.println(((currentTime - startTime) / reset) + "\t" + ((currentTime - startTime) % reset)/simulationLogTime + "\t" + (infected / Network.size()) + "\t" + (set.size() / (double)Network.size()));
    /*if (CommonState.getTime() >= CommonState.getEndTime() - 1000) {
      System.out.println("Model: " + vector);
      for (VectorEntry e : vector) {
        System.out.println(e.index + "\t" + e.value);
      }
    }*/
    
    return false;
  }

}
