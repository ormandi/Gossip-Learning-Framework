package gossipLearning.protocols;

import java.util.ArrayList;
import java.util.List;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class RapidLearningProtocol extends LearningProtocol {
  protected static final String PAR_MODELPROB = "initModelProbability";
  protected int numberOfSentModels;
  protected final double maxdelay;
  
  public RapidLearningProtocol(String prefix) {
    super(prefix);
    numberOfSentModels = 0;    
    maxdelay = (Configuration.getDouble("MAXDELAY")/Network.size()) * Configuration.getDouble("UNITSINSTEP"); ///((MobilTraceChurn)currentNode.getProtocol(churnProtocolID)).unitsInStep;
  }

  public RapidLearningProtocol(RapidLearningProtocol a) {
    super(a);
    numberOfSentModels = a.numberOfSentModels;
    maxdelay = a.maxdelay;
  }

  @Override
  public Object clone() {
    return new RapidLearningProtocol(this);
  }

  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        resultAggregator.push(currentProtocolID, i, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
      }
    }

    // send
    if (numberOfIncomingModels == 0) {
      numberOfWaits ++;
    } else {
      numberOfWaits = 0;
    }
    if (numberOfWaits == numOfWaitingPeriods) {
      numberOfIncomingModels = 1;
      numberOfWaits = 0;
    }
    for (int id = Math.min(numberOfIncomingModels-numberOfSentModels, capacity); id > 0; id --) {
      sendMessage(id);
    }
    numberOfIncomingModels = 0;
    numberOfSentModels = 0;
  }

  @Override
  protected void updateModels(ModelHolder modelHolder){
    //System.out.println(currentNode.getID() + " RECV");
    super.updateModels(modelHolder);
    sendMessage(1);
    numberOfSentModels ++;
    //System.out.println(currentNode.getID());
    //System.out.println(numberOfIncomingModels);
  }
  
  @Override
  protected void sendToRandomNeighbor(ModelMessage message) {
    Linkable overlay = getOverlay();
    List<Node> neighbors = new ArrayList<Node>();
    for (int i = 0; i < overlay.degree(); i++) {
      if (overlay.getNeighbor(i).isUp() && overlay.getNeighbor(i).getID() != currentNode.getID()){
        if (((LearningProtocol)overlay.getNeighbor(i).getProtocol(currentProtocolID)).getSessionLength()-maxdelay >= 0) {
          neighbors.add(overlay.getNeighbor(i));
        }
      }
    }
    if (neighbors.size() > 0) {
      Node randomNode = neighbors.get(CommonState.r.nextInt(neighbors.size()));
      getTransport().send(currentNode, randomNode, message, currentProtocolID);
    }
  }
  
}
