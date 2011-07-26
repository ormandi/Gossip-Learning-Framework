package gossipLearning.algorithms.pegasos;

import gossipLearning.algorithms.pegasos.model.PegasosModel;
import gossipLearning.interfaces.AbstractEDNode;
import gossipLearning.interfaces.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ModelQueueHolder;
import gossipLearning.messages.ActiveThreadMessage;
import gossipLearning.messages.ModelMessage;
import gossipLearning.messages.OnlineSessionFollowerActiveThreadMessage;
import gossipLearning.utils.Utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import p2pChurn.controls.ChurnControl;
import p2pChurn.interfaces.Churnable;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

@SuppressWarnings("rawtypes")
public class P2Pegasos extends AbstractEDNode implements EDProtocol,InstanceHolder<Map<Integer, Double>>,ModelHolder<PegasosModel>,ModelQueueHolder<PegasosModel>,Churnable {
  private static final String PAR_DELAYMEAN = "delayMean";
  protected double delayMean = Double.POSITIVE_INFINITY;
  private static final String PAR_DELAYVAR = "delayVar";
  protected double delayVar = 0.0;
  public static final String PAR_LAMBDA = "lambda";
  protected double lambda;
  
  protected Map<Integer, Double> x;                                    // instance (sparse)
  protected double y;                                                 // class label (+1.0 or -1.0)
  
  protected PegasosModel currentModel;
  protected Queue<PegasosModel> observedModels; // model queue
  
  private static final String PAR_MEMORYSIZE = "memory";
  protected int memorySize = 1;
  
  protected P2Pegasos() {
    observedModels = new LinkedList<PegasosModel>();
  }
  
  public P2Pegasos(String prefix) {
    this();
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 0.0);
    memorySize = Configuration.getInt(prefix + "." + PAR_MEMORYSIZE, 1);
  }
  
  public P2Pegasos clone() {
    P2Pegasos s = new P2Pegasos();
    s.lambda = lambda;
    s.delayMean = delayMean;
    s.delayVar = delayVar;
    s.memorySize = memorySize;
    return s;    
  }
  
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
    process(currentModel, currentNode, currentProtocolID, isUpdateAndStore, true);
    
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
    ModelMessage<PegasosModel> message = (ModelMessage<PegasosModel>) messageObj;
    
    //
    //-------------------- begin of work ------------------
    //
    
    // process incomming message
    currentModel = message.getModel();
    
    // do a gradient update on the received model and send it
    process(currentModel, currentNode, currentProtocolID, true, false);
    
    //
    //-------------------- end of work --------------------
    //
  }
  
  protected void process(PegasosModel model, Node currentNode, int currentProtocolID, boolean isUpdateAndStore, boolean isSend) {
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
  
  protected void sendModel(PegasosModel model, Node currentNode, int currentProtocolID) {
    // select a uniform random node and send my model to him
    Node neighbor = selectNeighbor();
    Transport transport = getTransport(currentNode, currentProtocolID);
    transport.send(currentNode, neighbor, new ModelMessage<PegasosModel>(currentNode, model), currentProtocolID);
  }
  
  protected void storeModel(PegasosModel model) {
    // store the new model in the limited sized model queue
    observedModels.offer(new PegasosModel(model.getW(), model.getBias(), model.getAge()));
    while (observedModels.size() > memorySize) {
      observedModels.poll();
    }
  }
  
  protected PegasosModel updateModel(PegasosModel model) {
    model.setAge(model.getAge() + 1);
    double nu = 1.0/(lambda * (double) (model.getAge()));
    boolean isSV = y * Utils.innerProduct(model.getW(), x) < 1.0;
    int max = findMaxIdx();
    for (int i = 0; i <= max; i ++) {
      Double wOldCompD = model.getW().get(i);
      Double xCompD = x.get(i);
      if (wOldCompD != null || xCompD != null) {
        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
        if (isSV) {
          // the current point in the current model is a SV
          // => applying the SV-based update rule
          model.getW().put(i, (1.0 - 1.0/((double)model.getAge())) * wOldComp + nu * y * xComp);
        } else {
          // the current point is not a SV in the currently stored model
          // => applying the normal update rule
          if (wOldCompD != null) {
            model.getW().put(i, (1.0 - 1.0/((double)model.getAge())) * wOldComp);
          }
        }
      }
    }
    return model;
  }
  
  protected Node selectNeighbor() {
    // selecting a uniform random neighbor to communicate with
    Linkable overlay = getOverlay();
    return overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
  }

  public Map<Integer, Double> getInstance() {
    return x;
  }
  
  public double getLabel() {
    return y;
  }
  
  public void setInstance(Map<Integer, Double> instance) {
    this.x = instance;
  }
  
  public void setLabel(double label) {
    if (label != -1.0 && label != 1.0) {
      throw new RuntimeException("Invalid class label at instance " + getCurrentNode().getID() + " which is " + label + "!");
    }
    this.y = label;
  }
  
  public PegasosModel getModel() {
    return currentModel;
  }
  
  public void setModel(PegasosModel model) {
    currentModel = (PegasosModel) model;
  }
  
  public void initModel() {
    currentModel = new PegasosModel(new TreeMap<Integer, Double>(), 0.0, 1);
  }
  
  protected int findMaxIdx() {
    int max = - Integer.MAX_VALUE;
    for (int d : currentModel.getW().keySet()) {
      if (d > max) {
        max = d;
      }
    }
    for (int d : x.keySet()) {
      if (d > max) {
        max = d;
      }
    }
    return max;
  }
  
  public Queue<PegasosModel> getModelQueue() {
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
