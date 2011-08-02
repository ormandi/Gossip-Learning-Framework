package gossipLearning.interfaces;

import gossipLearning.messages.ActiveThreadMessage;
import gossipLearning.messages.ModelMessage;
import gossipLearning.messages.OnlineSessionFollowerActiveThreadMessage;

import java.util.Queue;

import p2pChurn.controls.ChurnControl;
import p2pChurn.interfaces.Churnable;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

@SuppressWarnings("rawtypes")
public abstract class AbstractAlgorithm<I> extends AbstractEDNode implements EDProtocol,ModelHolder<I>,ModelQueueHolder<I>,Churnable {
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda;
  protected static final String PAR_DELAYMEAN = "delayMean";
  protected double delayMean = Double.POSITIVE_INFINITY;
  protected static final String PAR_DELAYVAR = "delayVar";
  protected double delayVar = 0.0;
  protected static final String PAR_MEMORYSIZE = "memory";
  protected int memorySize = 1;
  
  protected Model<I> currentModel;
  protected Queue<Model<I>> observedModels;                       // model queue
  
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    if ( messageObj instanceof ActiveThreadMessage ||
         ( messageObj instanceof OnlineSessionFollowerActiveThreadMessage && 
         ((OnlineSessionFollowerActiveThreadMessage)messageObj).sessionID == sessionID) ) {
      activeThread(currentNode, currentProtocolID, messageObj);
    } else if (messageObj instanceof ModelMessage) {
      passiveThread(currentNode, currentProtocolID, messageObj);
    }
  }
  
  protected void activeThread(Node currentNode, int currentProtocolID, Object messageObj) {
    // active thread => work & set new alarm
    
    //
    //-------------------- begin of work ------------------
    //
    
    // send a new model to a random neighbor
    //initModel(); // the existing model is stored in the model queue as well
    
    // at the first iteration we have a 0 model => it has to update.
    // later, when OnlineSessionMessage is received, the update is not neccessarily, since the model was updated when it received
    boolean isUpdateAndStore = messageObj instanceof ActiveThreadMessage;
    
    // do a gradient update on the brand new model and send it
    createModel(currentModel, currentNode, currentProtocolID, isUpdateAndStore, true);
    
    //
    //-------------------- end of work --------------------
    //
    
    
    // set new alarm
    if (!Double.isInfinite(delayMean)) {
      int delay = (int)(delayMean + CommonState.r.nextGaussian()*delayVar);
      delay = (delay > 0) ? delay : 1;
      EDSimulator.add(delay, new OnlineSessionFollowerActiveThreadMessage(sessionID), currentNode, currentProtocolID);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected void passiveThread(Node currentNode, int currentProtocolID, Object messageObj) {
    // passive thread => receive & process incomming message
    ModelMessage<I> message = (ModelMessage<I>) messageObj;
    
    //
    //-------------------- begin of work ------------------
    //
    
    // process incomming message
    currentModel = message.getModel();
    
    // do a gradient update on the received model and send it
    createModel(currentModel, currentNode, currentProtocolID, true, false);
    
    //
    //-------------------- end of work --------------------
    //
  }
  
  protected void createModel(Model<I> model, Node currentNode, int currentProtocolID, boolean isUpdateAndStore, boolean isSend) {
    if (isUpdateAndStore) {
      // update the model
      updateModel(model);
      // store model in the model queue
      storeModel(model);
    }
    
    if (isSend ) {
      // send model to a random neighbor
      sendModel(model, currentNode, currentProtocolID);
    }
  }
  
  abstract protected void sendModel(Model<I> model, Node currentNode, int currentProtocolID);
  abstract protected void storeModel(Model<I> model);
  abstract protected Model<I> updateModel(Model<I> model);
    
  protected Node selectNeighbor() {
    // selecting a uniform random neighbor to communicate with
    Linkable overlay = getOverlay();
    return overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
  }
  
  public Model<I> getModel() {
    return currentModel;
  }
  
  public void setModel(Model<I> model) {
    currentModel = model;
  }
  
  public Queue<Model<I>> getModelQueue() {
    return observedModels;
  }
  
  public int getMemorySize() {
    return memorySize;
  }
  
  protected long sessionLength = ChurnControl.INIT_SESSION_LENGTH;
  protected int sessionID = 0;
  public long getSessionLength() {
    return sessionLength;
  }

  public void setSessionLength(long sessionLength) {
    this.sessionLength = sessionLength;
  }
  
  public void init(Node currentNode, int currentProtocolID) {
    sessionID ++;
    EDSimulator.add(0, new OnlineSessionFollowerActiveThreadMessage(sessionID), currentNode, currentProtocolID);
  }
}
