package gossipLearning.protocols;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gossipLearning.controls.ChurnControl;
import gossipLearning.controls.MobilTraceChurnForSoloWalker;
import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SoloLearningModel;
import gossipLearning.interfaces.protocols.DimensionReductionProtocol;
import gossipLearning.interfaces.protocols.HotPotatoProtocol;
import gossipLearning.interfaces.protocols.InstanceLoaderConnection;
import gossipLearning.messages.ConnectionTimeoutMessage;
import gossipLearning.messages.Message;
import gossipLearning.messages.RestartableSoloModelMessage;
import gossipLearning.messages.WaitingMessage;
import gossipLearning.utils.CurrentRandomWalkStatus;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.MessageMap;
import gossipLearning.utils.RandomWalkProperties;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class SoloWalkerProtocol implements HotPotatoProtocol,Cloneable,InstanceLoaderConnection {

  private static final String PAR_EXTRACTORPID = "extractorProtocol";
  protected static final String PAR_ARRGNAME = "aggrName";
  private static final String PAR_MODELNAME = "modelName";
  private static final String PAR_EVALNAME = "evalName";
  private static final String PAR_EVALPROB = "evalProbability";

  // state variables
  protected Node currentNode;
  protected int currentProtocolID = -1;

  protected final int exrtactorProtocolID;
  protected ResultAggregator resultAggregator;
  protected final double evaluationProbability;
  protected final String modelName;
  protected final String aggrClassName;
  protected Model latestModel;
  protected final String evalName;

  // variables for modeling churn
  protected long sessionLength = ChurnControl.INIT_SESSION_LENGTH;
  protected int sessionID = 0;

  protected static final long MESSAGE_DELAY = Configuration.getLong("MESSAGEDELAY");  // model message delay
  protected static final long MIN_MESSAGE_LATENCY  = Configuration.getLong("MINLATENCYDELAY"); // minimum massage latency
  private static final double MODEL_SURVIVE_PROBABILITY  = Configuration.getDouble("MODELSURVIVEPROBABILITY");
  public static final long RANDOMWALK_TIME_LIMIT = Configuration.getLong("RANDOMWALKTIMELIMIT");    // time indicates that the random walk is dead

  private static boolean isGlobalFirstModelCreated = false;
  public RandomWalkProperties rwprop;
  public RandomWalkProperties rwpropNC;
  protected MessageMap messageMap;
  public long currentTimeLimit;
  public double numberOfRestartNodes;
  private boolean myFirstModelInited;
  private Integer ownStepID;

  public static Map<Integer, CurrentRandomWalkStatus > knownRandomWalks = new HashMap<Integer, CurrentRandomWalkStatus>();

  public SoloWalkerProtocol(String prefix) {
    // loading configuration parameters
    exrtactorProtocolID = Configuration.getPid(prefix + "." + PAR_EXTRACTORPID);
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    evalName = Configuration.getString(prefix + "." + PAR_EVALNAME);
    aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
    evaluationProbability = Configuration.getDouble(prefix + "." + PAR_EVALPROB, 1.0);
    // setting up learning related variables
    try {
      String[] modelNames = {modelName};
      String[] evalNames = {evalName};
      resultAggregator = (ResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
      latestModel = (Model)Class.forName(modelName).getConstructor(String.class).newInstance(prefix);
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
    myFirstModelInited = false;
    rwprop = new RandomWalkProperties();
    rwpropNC = new RandomWalkProperties();
    messageMap = new MessageMap();
    currentTimeLimit = RANDOMWALK_TIME_LIMIT;
    numberOfRestartNodes = 1.0;
    ownStepID = 0;
  }

  public SoloWalkerProtocol(SoloWalkerProtocol a) {
    exrtactorProtocolID = a.exrtactorProtocolID;
    modelName = a.modelName;
    evalName = a.evalName;
    aggrClassName = a.aggrClassName;
    evaluationProbability = a.evaluationProbability; 
    resultAggregator = (ResultAggregator)a.resultAggregator.clone();
    latestModel = (Model)a.latestModel.clone();
    myFirstModelInited = a.myFirstModelInited;
    rwprop = a.rwprop;
    rwpropNC = a.rwpropNC;
    messageMap = (MessageMap) a.messageMap.clone();
    currentTimeLimit = a.currentTimeLimit;
    numberOfRestartNodes = a.numberOfRestartNodes;
    ownStepID = a.ownStepID;
  }

  @Override
  public Object clone() {
    return new SoloWalkerProtocol(this);
  }

  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    // the current node and protocol fields are updated
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    if (messageObj instanceof RestartableSoloModelMessage) {
      // The received message is a model message => calling the passive thread handler
      if(CommonState.r.nextDouble() <= MODEL_SURVIVE_PROBABILITY){
        onReceiveRandomWalk((RestartableSoloModelMessage)messageObj);
      }
    } else if (messageObj instanceof WaitingMessage) {
      WaitingMessage waitingMessage = ((WaitingMessage) messageObj);
      if (waitingMessage.isWakeUpMessage()) {
        rwprop = pullRWPropFromAnRandomNeighbor();
        if(!messageMap.isEmpty()){
          wakeUpResendMessage();
        }
      } else {
        Integer iD = waitingMessage.getId();
        if(messageMap.containsKey(iD)){
          Node neighbor = messageMap.getNode(iD);
          if (neighbor.isUp()) {
            sendMessageImmediately(currentNode, neighbor, messageMap.remove(iD), currentProtocolID);
            removeRandomWalkFromGlobalTable(iD);
          } else {
            Message unSendedMessage = messageMap.remove(iD);
            if(unSendedMessage instanceof RestartableSoloModelMessage){
              resendMessage(unSendedMessage,iD,MESSAGE_DELAY);       
            }
          }
        }
      }
    } else if (messageObj instanceof ConnectionTimeoutMessage){
      ConnectionTimeoutMessage tom = ((ConnectionTimeoutMessage) messageObj);
      Node newOflineNode = tom.getSource();
      if(messageMap.containsValue(newOflineNode)){
        Set<Integer> messageIDs = messageMap.getMessageIDs(newOflineNode);
        for (Integer ID : messageIDs) {
          resendMessage(messageMap.remove(ID), ID, MESSAGE_DELAY);
        }        
      }
    } 
  }

  @Override
  public void nextCycle(Node currentNode, int currentProtocolID) {
    this.currentNode = currentNode;
    MobilTraceChurnForSoloWalker.numberOfNextCycle++;
    this.currentProtocolID = currentProtocolID;
    if( isAnyOnlineNeighbor() ){
      if (!isGlobalFirstModelCreated && sessionLength > MESSAGE_DELAY) {
        isGlobalFirstModelCreated = true;
        myFirstModelInited = true;
        initModels(true);
        SoloLearningModel latestSoloLearningModel = (SoloLearningModel)latestModel;
        Integer messageID = nextIdForMessageMap();
        knownRandomWalks.put(messageID, new CurrentRandomWalkStatus(currentNode.getID(), latestSoloLearningModel.getStepID(), latestSoloLearningModel.getModelID(), latestSoloLearningModel.getAge(), true));
        ownStepID = latestSoloLearningModel.getStepID();
        sendMessage(messageID,true,0);
      }
      if (!myFirstModelInited) {
        initModels(false);
        myFirstModelInited = true;
      }
      if(rwprop.getAge() >= currentTimeLimit && rwprop.getStep() >= 0){
        if(!messageMap.isSendingThisStepID(rwprop.getStepID())) {
          SoloLearningModel latestSoloLearningModel = (SoloLearningModel)latestModel;
          double diffFromMaxStep = rwprop.getStep() - latestSoloLearningModel.getStep();
          if(diffFromMaxStep <= numberOfRestartNodes ) {
            int stepID =  ((SoloLearningModel)latestSoloLearningModel).initIDs();
            int modelID = ((SoloLearningModel)latestSoloLearningModel).getModelID();
            int restartedStepID = rwprop.getStepID();
            Integer messageID = nextIdForMessageMap();
            knownRandomWalks.put(messageID, new CurrentRandomWalkStatus(currentNode.getID(), stepID, modelID, latestSoloLearningModel.getStep(), true));
            ownStepID = stepID;
            sendMessage(messageID,restartedStepID);
          }
        } 
        numberOfRestartNodes+=1.0;
        currentTimeLimit+=RANDOMWALK_TIME_LIMIT;
      }
      if(rwprop.getAge() < RANDOMWALK_TIME_LIMIT){
        currentTimeLimit = RANDOMWALK_TIME_LIMIT;
        numberOfRestartNodes = 1.0;
      }
      rwprop = pushPullRWPropWithAnRandomNeighbor();
      rwpropNC = new RandomWalkProperties(rwprop);
    } 
  }

  private RandomWalkProperties pullRWPropFromAnRandomNeighbor() {
    Node neighborNode = getRandomNeighbor();
    SoloWalkerProtocol neighbor = ((SoloWalkerProtocol)neighborNode.getProtocol(currentProtocolID));
    return mergeProp(rwprop,neighbor.rwprop);
  }

  private RandomWalkProperties pushPullRWPropWithAnRandomNeighbor() {
    Node neighborNode = getRandomNeighbor();
    SoloWalkerProtocol neighbor = ((SoloWalkerProtocol)neighborNode.getProtocol(currentProtocolID));
    neighbor.rwpropNC = mergeProp(neighbor.rwpropNC,rwprop);
    rwpropNC = mergeProp(rwpropNC,neighbor.rwprop);      
    return mergeProp(rwprop,rwpropNC);
  }

  private RandomWalkProperties mergeProp(RandomWalkProperties currentRWProp, RandomWalkProperties receivedRWProp) {
    long recRWpropEstimatedTime = CommonState.getTime()-(receivedRWProp.getAge()+getRandomLatency());
    int currentStep = currentRWProp.getStep();
    int receivedStep = receivedRWProp.getStep();
    long currentAge = currentRWProp.getAge();
    long receivedAge =  receivedRWProp.getAge();
    if ( (currentStep <= receivedStep && currentAge <  receivedAge &&  receivedAge < RANDOMWALK_TIME_LIMIT ) ||
        (currentStep <= receivedStep && currentAge >= receivedAge) ||
        (currentStep >  receivedStep && currentAge >= receivedAge + RANDOMWALK_TIME_LIMIT) ||
        (currentStep >  receivedStep && currentAge <  receivedAge + RANDOMWALK_TIME_LIMIT 
            && currentAge >= RANDOMWALK_TIME_LIMIT && receivedAge < RANDOMWALK_TIME_LIMIT) ||
        (receivedRWProp.getStepID() == currentRWProp.getStepID() && currentRWProp.getRwPropStep() > receivedRWProp.getRwPropStep()+1)){
      return new RandomWalkProperties(receivedRWProp.getStepID(),
          recRWpropEstimatedTime,
          receivedStep,
          receivedRWProp.getRwPropStep()+1);
    } else {
      return currentRWProp;
    }
  }

  private Long getRandomLatency(){
    return Math.round(CommonState.r.nextLong((int)MIN_MESSAGE_LATENCY+1)-MIN_MESSAGE_LATENCY+(MIN_MESSAGE_LATENCY/2.0));
  }

  @Override
  public void onReceiveRandomWalk(Message messageObj) {
    RestartableSoloModelMessage message = (RestartableSoloModelMessage)messageObj;
    if (message.getTargetPid() == currentProtocolID) {
      // get instances from the extraction protocol
      Model receivedModel = message.getModel();
      InstanceHolder instances = ((DimensionReductionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
      SoloLearningModel receivedSoloLearningModel = (SoloLearningModel)receivedModel;
      SoloLearningModel latestSoloLearningModel = (SoloLearningModel)latestModel;
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        receivedSoloLearningModel.update(x, y);
      }
      receivedSoloLearningModel = receivedSoloLearningModel.merge(latestSoloLearningModel);
      if (rwprop.getStep() < receivedSoloLearningModel.getStep() || 
          rwprop.getAge() >= RANDOMWALK_TIME_LIMIT ||
          message.getRestartedStepId() == rwprop.getStepID()) {
        Integer messageID = nextIdForMessageMap();
        int stepID = receivedSoloLearningModel.getStepID();
        int modelID = receivedSoloLearningModel.getModelID(); 
        rwprop = new RandomWalkProperties(stepID,CommonState.getTime(),(int)receivedSoloLearningModel.getStep(),0);
        rwpropNC = new RandomWalkProperties(rwprop);
        ownStepID = stepID;
        knownRandomWalks.put(messageID, new CurrentRandomWalkStatus(currentNode.getID(), stepID, modelID, receivedSoloLearningModel.getAge(), true));
        // stores the updated model
        latestModel = (SoloLearningModel) receivedSoloLearningModel.clone();;
        sendMessage(messageID,message.getRestartedStepId());
      } else {
        // random walk is shut down
      }

    }
  }


  protected void initModels(boolean isSending){
    SoloLearningModel modelMergeAble = (SoloLearningModel)latestModel;
    if(isSending){
      modelMergeAble.initIDs();
    } else {
      modelMergeAble.init();
    }
    latestModel=modelMergeAble;
  }

  public Integer nextIdForMessageMap(){
    Integer id = CommonState.r.nextInt(); 
    while(messageMap.containsKey(id) || knownRandomWalks.containsKey(id) || id == 0){
      id = CommonState.r.nextInt();
    }
    return id;
  }

  public void proxySend(Message message, Node dest, WaitingMessage wm, long delay){
    EDSimulator.add(delay, wm, currentNode, currentProtocolID);
    messageMap.put(wm.getId(), message, dest);
  }

  public void proxySend(Message message, Node dest, Integer id, long delay){
    WaitingMessage wm = new WaitingMessage(id);
    proxySend(message, dest, wm, delay);
  }

  private void proxySendToRandomNeighbor(Message message, Integer id, long delay) {
    Node randomNeighborForNewConnection = getRandomNeighbor();
    proxySend(message,randomNeighborForNewConnection, id, delay);
  }

  private void resendMessage(Message message, Integer id, long delay) {
    proxySendToRandomNeighbor(message, id, delay);
  }

  private void sendMessageImmediatelyToRandomNeighbor(Node currentNode, Message message, int currentProtocolID) {
    sendMessageImmediately(currentNode, getRandomNeighbor(), message, currentProtocolID);
  }

  private void sendMessageImmediately(Node currentNode, Node neighbor, Message message, int currentProtocolID) {
    getTransport().send(currentNode, neighbor, message, currentProtocolID);
  }

  private void wakeUpResendMessage(){
    Set<Integer> oldIds = new HashSet<Integer>(messageMap.keySet());
    for (Integer iD : oldIds) {
      Message m = messageMap.remove(iD);
      if (m instanceof RestartableSoloModelMessage){
        Model model = ((RestartableSoloModelMessage) m).getModel();
        boolean isResended = false;
        if (rwprop.getStep() <= model.getAge()) {
          resendMessage(m, iD, MESSAGE_DELAY);
          isResended = true;
        } 
        if (!isResended) {
          removeRandomWalkFromGlobalTable(iD);
        }
      }
    }
  }

  private CurrentRandomWalkStatus removeRandomWalkFromGlobalTable(Integer iD){
    ownStepID = 0;
    return knownRandomWalks.remove(iD);
  }

  public void sendMessage(int messageID, int restartedStepID){
    sendMessage(messageID, true, restartedStepID);
  }

  public void sendMessage(int messageID, boolean isProxySend, int restartedStepID) {
    // send the latest models to a random neighbor
    RestartableSoloModelMessage message = new RestartableSoloModelMessage(currentNode, latestModel, currentProtocolID, restartedStepID);
    if(isProxySend){
      proxySendToRandomNeighbor(message, messageID, MESSAGE_DELAY);
    } else {
      sendMessageImmediatelyToRandomNeighbor(currentNode, message, currentProtocolID);
      removeRandomWalkFromGlobalTable(messageID);
    }
  }

  protected Node getRandomNeighbor() {
    Linkable overlay = getOverlay();
    //List<Node> onlineNodes = new ArrayList<Node>();
    for (int i = 0; i < overlay.degree()*2; i++) {
      Node randomOnlineNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
      if(randomOnlineNode.isUp() && randomOnlineNode.getID()!=currentNode.getID()) {
        return randomOnlineNode;
      }
    }
    return currentNode;
  }

  private boolean isAnyOnlineNeighbor() {
    for (int i = 0; i < getOverlay().degree(); i++) {
      if (getOverlay().getNeighbor(i).isUp()) {
        return true;
      }
    }  
    return false;
  }

  public int getNumberOfOutConnection(){
    return messageMap.size();
  }

  /**
   * It is method which makes more easer of the accessing to the transport layer of the current node.
   * 
   * @return The transform layer is returned.
   */
  protected Transport getTransport() {
    return ((Transport) currentNode.getProtocol(FastConfig.getTransport(currentProtocolID)));
  }

  /**
   * This method supports the accessing of the overlay of the current node.
   * 
   * @return The overlay of the current node is returned.
   */
  protected Linkable getOverlay() {
    return (Linkable) currentNode.getProtocol(FastConfig.getLinkable(currentProtocolID));
  }

  /**
   * This is a helper method which returns the current protocol instance.
   * 
   * @return This protocol instance is returned.
   */
  protected HotPotatoProtocol getCurrentProtocol() {
    return (HotPotatoProtocol) currentNode.getProtocol(currentProtocolID);
  }

  /**
   * Returns the ID of the current protocol.
   * @return the protocol ID
   */
  public int getPID() {
    if (currentProtocolID < 0) {
      throw new RuntimeException("Too early request for PID!");
    }
    return currentProtocolID;
  }

  //----- Churnable related methods -----

  /**
   * Basic churn implementation which simply stores the remaining session length
   * in a field.
   * 
   * @return remaining session length
   */
  @Override
  public long getSessionLength() {
    return sessionLength;
  }

  /**
   * It sets a new session length.
   * 
   * @param new session length which overwrites the original value 
   */
  @Override
  public void setSessionLength(long sessionLength) {
    this.sessionLength = sessionLength;
  }

  /**
   * Session initialization simply awakes the protocol by adding an active thread event to itself
   * with delay 0.
   */
  @Override
  public void initSession(Node node, int protocol) {
    sessionID ++;
  }

  /**
   * Returns the aggregated result aggregate of this node.
   * @return the aggregated result.
   */
  public ResultAggregator getResults() {
    return resultAggregator;
  }

  /**
   * Sets the specified number of classes for the models.
   * @param numberOfClasses the number of classes to be set
   */
  public void setNumberOfClasses(int numberOfClasses) {
    ((SoloLearningModel)latestModel).setNumberOfClasses(numberOfClasses);
  }

}
