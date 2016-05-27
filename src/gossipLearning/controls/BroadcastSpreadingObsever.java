package gossipLearning.controls;

import java.util.ArrayList;
import java.util.List;

import gossipLearning.protocols.SoloWalkerProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class BroadcastSpreadingObsever extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;

  protected final long logTime;


  public BroadcastSpreadingObsever(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    logTime = Configuration.getLong("simulation.logtime");
  }

  public boolean execute() {
    updateGraph();
    List<Integer> stepOccurrence = new ArrayList<Integer>();
    stepOccurrence.add(0);
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      if (node.isUp()){
        SoloWalkerProtocol swp = (SoloWalkerProtocol)node.getProtocol(pid);
        Integer knownStep = swp.rwprop.getStep();
        if (knownStep < 0) {
          knownStep = 0;
        }
        if(knownStep>=stepOccurrence.size()){
          for (int j = stepOccurrence.size(); j < knownStep; j++) {
            stepOccurrence.add(0);
          }
          stepOccurrence.add(1);
        } else {
          stepOccurrence.set(knownStep, stepOccurrence.get(knownStep)+1);
        }
        /*if (stepOccurrence.size()>1 && knownStep < 1 ) {
          Linkable overlay = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
          String neighStr = "";
          for (int j = 0; j < overlay.degree(); j++) {
            Node neighbor = overlay.getNeighbor(j);
            if(neighbor.isUp()){
              neighStr+=" "+neighbor.getID();
            }
          }
          System.err.println("\t"+node.getID()+" "+swp.rwprop.getAge()+" "+swp.rwprop.getRwPropStep()+" "+swp.rwprop.getStepID()+neighStr);
        }*/
      }
    }
    String outStr = "";
    for (int i = 0; i < stepOccurrence.size(); i++) {
      outStr += " " + stepOccurrence.get(i);
    }
    System.out.println(outStr);
    return false;
  }
}
