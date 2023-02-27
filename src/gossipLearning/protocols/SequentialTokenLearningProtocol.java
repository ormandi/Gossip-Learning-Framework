package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partitioned;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.*;
import java.util.*;

/**
 * PartitionedTokenLearningProtocol with sequential message sending.
 */
public class SequentialTokenLearningProtocol extends PartitionedTokenLearningProtocol {

  //---------------------------------------------------------------------
  //Parameters
  //---------------------------------------------------------------------
  
  /** 
   * The latency.
   * @config
   */	
  private static final String PAR_TRANS = "transmissionTime";
  
  //---------------------------------------------------------------------
  //Fields
  //---------------------------------------------------------------------

  final long transmissionTime;
  Queue<Integer> messageQueue = new ArrayDeque<Integer>();
  long EOT;
  
  //---------------------------------------------------------------------
  //Initialization
  //---------------------------------------------------------------------

  /**
   * Constructor for reading configuration parameters.
   */
  public SequentialTokenLearningProtocol(String prefix) {
    super(prefix);
    transmissionTime = Configuration.getLong(prefix + "." + PAR_TRANS);
  }

  /**
   * Copy constructor.
   */
  public SequentialTokenLearningProtocol(SequentialTokenLearningProtocol a) {
    super(a);
    transmissionTime = a.transmissionTime;
  }
  
  @Override
  public SequentialTokenLearningProtocol clone() {
    return new SequentialTokenLearningProtocol(this);
  }

  //---------------------------------------------------------------------
  //Methods
  //---------------------------------------------------------------------
  
  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    if (messageObj instanceof Integer) {
      trySend();
    } else {
      super.processEvent(currentNode, currentProtocolID, messageObj);
    }
  }
  
  @Override
  protected void sendMsg(int pi) {
    --token[pi];
    assert token[pi]>=0;
    messageQueue.add(pi);
    trySend();
  }
  
  private void trySend() {
    if (EOT<=CommonState.getTime()&&!messageQueue.isEmpty()) {
      if (!nodeIsOnline(currentNode,currentProtocolID)) {
        while (!messageQueue.isEmpty()) {
          int pi = messageQueue.remove();
          if (token[pi]<B)
            token[pi]++;
        }
        return;
      }
      int pi = messageQueue.remove();
      EOT = CommonState.getTime()+transmissionTime;
      EDSimulator.add(transmissionTime,0,currentNode,currentProtocolID);
      modelHolder.clear();
      for (int i=0; i<models.length; i++) {
        Model model = models[i];
        model = ((Partitioned)model).getModelPart(pi);
        modelHolder.add(model);
      }
      // send the latest models to a random online neighbor
      Linkable linkable = getOverlay();
      if (permMode==0)
        neighborPerm.reset(linkable.degree());
      for (int i=0; i<2; i++) {
        while (neighborPerm.hasNext()) {
          Node target = linkable.getNeighbor(neighborPerm.next());
          if (nodeIsOnline(target,currentProtocolID)) {
            getTransport().send(currentNode,target,new PartitionedTokenMessage(currentNode,target,modelHolder,currentProtocolID,true,pi),currentProtocolID);
            return;
          }
        }
        neighborPerm.reset(linkable.degree());
      }
    }
  }
  
}
