package gossipLearning.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import gossipLearning.controls.ChurnControl;
import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.MultiLearningModel;
import gossipLearning.interfaces.protocols.DimensionReductionProtocol;
import gossipLearning.interfaces.protocols.HotPotatoProtocol;
import gossipLearning.interfaces.protocols.InstanceLoaderConnection;
import gossipLearning.messages.EventMessage;
import gossipLearning.messages.EventWithModelInfoMessage;
import gossipLearning.messages.Message;
import gossipLearning.messages.RestartableModelMessage;
import gossipLearning.utils.EventEnum;
import gossipLearning.utils.EventWithModelInfoEnum;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.ModelContainer;
import gossipLearning.utils.ModelInfo;
import gossipLearning.utils.SendBroadcastInfo;
import gossipLearning.utils.SendModelInfo;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.SuperviseNodeContainer;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class MultiWalkerProtocol implements HotPotatoProtocol,Cloneable,InstanceLoaderConnection {

  protected static final String PAR_EXTRACTORPID = "extractorProtocol";
  protected static final String PAR_BROADCASTLEVEL = "BROADCASTLEVEL";
  protected static final String PAR_ARRGNAME = "aggrName";
  protected static final String PAR_MODELNAME = "modelName";
  protected static final String PAR_EVALNAME = "evalName";
  protected static final String PAR_NUMQUEUE = "numberOfMaxModelInQueue";

  public static final int BROADCAST_LEVEL = Configuration.getInt(PAR_BROADCASTLEVEL);
  public static final long MIN_MESSAGE_LATENCY  = Configuration.getLong("MINLATENCYDELAY");
  public static final long OFFLINE_DELAY  = Configuration.getLong("OFFLINE_DELAY");
  protected static final long TIMEWINDOW_FOR_RESTART = Configuration.getLong("TIMEWINDOW");


  public static int numberOfRestartInLevel1 = 0;
  public static int numberOfRestartAttemptInLevel2 = 0;
  public static int numberOfRestartInLevel2 = 0;
  public static long numberOfSupervisionInLevel2 = 0L;
  public static long numberOfSupervisionInLevel1 = 0L;

  //public static HashMap<Node, HashSet<Integer>> modelObserverMap = new HashMap<Node, HashSet<Integer>>();

  protected final String prefix;
  protected final String modelName;
  protected final String aggrClassName;
  protected final String evalName;
  protected Node currentNode;
  protected int currentProtocolID;
  protected int exrtactorProtocolID;
  protected ResultAggregator resultAggregator;
  protected boolean isFirstSessionOnline;
  protected boolean isFirstInited;
  protected int numberOfMaxModelInQueue;

  protected long sessionLength = ChurnControl.INIT_SESSION_LENGTH;
  protected int sessionID = 0;

  protected ModelContainer storageQueue;
  protected LinkedList<MultiLearningModel> sendQueue;
  protected SendModelInfo sendInfo;
  protected HashMap<SendBroadcastInfo, SendBroadcastInfo> sendBroadcastInfoSet;
  protected HashMap<SendBroadcastInfo, SendBroadcastInfo> sendBroadcastInfoSetForNextCycle;
  protected HashMap<Long,MultiLearningModel> restartCandidateModelSet;
  protected HashMap<Long,ModelInfo> restartCandidateModelInfoSet;
  protected HashMap<ModelInfo, Node> responsibilityForFeedback;
  protected SuperviseNodeContainer<MultiLearningModel> superviseNodeAndThePotentialRestartModel;  
  protected HashMap<ModelInfo, Node> responsibilityForFeedback2Level;

  protected SuperviseNodeContainer<ModelInfo> superviseNodeLevel2;
  protected long offTime;

  protected HashSet<ModelInfo> removeModelInfos;
  protected HashSet<Long> removeNodeID;
  
  protected HashMap<Long,Long> connectionTimeOuts;
  protected HashMap<Long, Node> connectionTimeOutsToNode;

  public MultiWalkerProtocol(String prefix) {
    this.prefix = prefix;
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
    evalName = Configuration.getString(prefix + "." + PAR_EVALNAME);
    exrtactorProtocolID = Configuration.getPid(prefix + "." + PAR_EXTRACTORPID);
    numberOfMaxModelInQueue = Configuration.getInt(prefix + "." + PAR_NUMQUEUE);
    String[] modelNames = {modelName};
    String[] evalNames = {evalName};
    try {
      resultAggregator = (ResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
    isFirstSessionOnline = false;
    isFirstInited = false;
    storageQueue = new ModelContainer(prefix);
    sendQueue = new LinkedList<MultiLearningModel>();
    nextProxySendModelFromTheQueue(); //init sendInfo
    sendBroadcastInfoSet = new HashMap<SendBroadcastInfo, SendBroadcastInfo>();
    sendBroadcastInfoSetForNextCycle = new HashMap<SendBroadcastInfo, SendBroadcastInfo>();
    restartCandidateModelSet = new HashMap<Long,MultiLearningModel>();
    restartCandidateModelInfoSet = new HashMap<Long,ModelInfo>();    
    responsibilityForFeedback = new HashMap<ModelInfo, Node>();
    superviseNodeAndThePotentialRestartModel = new SuperviseNodeContainer<MultiLearningModel>();
    responsibilityForFeedback2Level = new HashMap<ModelInfo, Node>();
    superviseNodeLevel2 = new SuperviseNodeContainer<ModelInfo>();
    offTime=0L;
    removeModelInfos = new HashSet<ModelInfo>();
    removeNodeID = new HashSet<Long>();
    connectionTimeOuts = new HashMap<Long,Long>(); 
    connectionTimeOutsToNode = new HashMap<Long,Node>(); 
  }

  @SuppressWarnings("unchecked")
  public MultiWalkerProtocol(MultiWalkerProtocol o) {
    prefix = o.prefix;
    modelName = o.modelName;
    aggrClassName = o.aggrClassName;
    evalName = o.evalName;
    currentNode = o.currentNode;
    currentProtocolID = o.currentProtocolID;
    exrtactorProtocolID = o.exrtactorProtocolID;
    resultAggregator = (ResultAggregator)o.resultAggregator.clone();
    isFirstSessionOnline = o.isFirstSessionOnline;
    isFirstInited = o.isFirstInited;
    numberOfMaxModelInQueue = o.numberOfMaxModelInQueue;
    sessionLength = o.sessionLength;
    storageQueue = (ModelContainer)o.storageQueue.clone();
    sendQueue = new LinkedList<MultiLearningModel>(); sendQueue.addAll(o.sendQueue);
    sendInfo = (SendModelInfo)o.sendInfo;
    sendBroadcastInfoSet = new HashMap<SendBroadcastInfo, SendBroadcastInfo>(); sendBroadcastInfoSet.putAll(o.sendBroadcastInfoSet);
    sendBroadcastInfoSetForNextCycle = new HashMap<SendBroadcastInfo, SendBroadcastInfo>(); sendBroadcastInfoSetForNextCycle.putAll(o.sendBroadcastInfoSetForNextCycle);
    restartCandidateModelSet = new HashMap<Long, MultiLearningModel>(); restartCandidateModelSet.putAll(o.restartCandidateModelSet);
    restartCandidateModelInfoSet = new HashMap<Long, ModelInfo>(); restartCandidateModelInfoSet.putAll(o.restartCandidateModelInfoSet);
    responsibilityForFeedback = new HashMap<ModelInfo, Node>(); responsibilityForFeedback.putAll(o.responsibilityForFeedback);
    superviseNodeAndThePotentialRestartModel = (SuperviseNodeContainer<MultiLearningModel>)o.superviseNodeAndThePotentialRestartModel.clone();
    responsibilityForFeedback2Level = new HashMap<ModelInfo, Node>(); responsibilityForFeedback2Level.putAll(o.responsibilityForFeedback2Level);
    superviseNodeLevel2 = (SuperviseNodeContainer<ModelInfo>)o.superviseNodeLevel2.clone();
    offTime=o.offTime;
    removeModelInfos = new HashSet<ModelInfo>();
    removeNodeID = new HashSet<Long>();
    connectionTimeOuts = new HashMap<Long,Long>(); connectionTimeOuts.putAll(o.connectionTimeOuts);
    connectionTimeOutsToNode = new HashMap<Long,Node>(); connectionTimeOutsToNode.putAll(o.connectionTimeOutsToNode);
  }

  @Override
  public Object clone() {
    return new MultiWalkerProtocol(this);
  }

  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    if (messageObj instanceof EventMessage){
      EventMessage message = (EventMessage)messageObj;
      if (message.getEvent() == EventEnum.WakeUpAndSend){ //true if WAKEUPANDSEND event is occurred and if there is no restart
        if(sendInfo.isSending()){
          Node dest = sendInfo.getDest();
          if(message.getDest().getID() == dest.getID()){
            if (dest.isUp()) {
              if(offTime <= 0) {
                sendMessage(dest); 
              } else {
                System.err.println("Most offdelay waiting "+CommonState.getTime()+" "+offTime);
                proxySendModelWithOffTimeDelay(sendInfo.getModelToBeSent(),dest,offTime);
              }
            } else {
              addToConnectionTimeOutCandidate(dest);
            }
          }
        }
      } else if(message.getEvent() == EventEnum.ConnectionTimeout ) {
        addToConnectionTimeOutCandidate(message.getSrc());
      } else if(message.getEvent() == EventEnum.WakeUp) {
        // store models from modelcontainer
        for (MultiLearningModel sendModel : sendQueue) {
          storageQueue.add(sendModel);
        }
        if (sendInfo.isSending()){
          storageQueue.add(sendInfo.getModelToBeSent());
        }
        // kill all previous process
        resetAllContainer();
      }
    } else if (messageObj instanceof EventWithModelInfoMessage){
      EventWithModelInfoMessage message = (EventWithModelInfoMessage)messageObj;
      if(message.getEvent() == EventWithModelInfoEnum.StartSupervisingMeInLevel2){
        message.getModelInfo().addNoiseToLastSeenTimeStamp();
        ModelInfo remainModelInfo = superviseNodeLevel2.add(message.getSrc(), message.getModelInfo().getModelID(), message.getModelInfo());
        isConcurrentModelOccured(remainModelInfo, message.getModelInfo());
      } else if(message.getEvent() == EventWithModelInfoEnum.StopSupervisingTheMessageHasBeenSent ) {
        superviseNodeAndThePotentialRestartModel.removeFromInnerSetByModelID(message.getSrc().getID(), message.getModelInfo().getModelID());
      } else if(message.getEvent() == EventWithModelInfoEnum.StopSupervisingTheMessageHasBeenSentLevel2 ) {
        superviseNodeLevel2.removeFromInnerSetByModelID(message.getSrc().getID(),message.getModelInfo().getModelID());
      }
    } else if (messageObj instanceof RestartableModelMessage) {
      onReceiveRandomWalk((Message)messageObj);
    }
  }

  private void addToConnectionTimeOutCandidate(Node dest) {
    if(!connectionTimeOuts.containsKey(dest.getID())){
      connectionTimeOuts.put(dest.getID(), CommonState.getTime()+OFFLINE_DELAY);
      connectionTimeOutsToNode.put(dest.getID(), dest);
    }
  }

  private void isConcurrentModelOccured(ModelInfo remainModelInfo, ModelInfo prevModelInfo) {
    if(remainModelInfo.getWalkID() != prevModelInfo.getWalkID()){
      SendBroadcastInfo sbi = new SendBroadcastInfo(remainModelInfo, EventWithModelInfoEnum.ConcurrentModelsOccured);
      sendBroadcastInfoSetForNextCycle.put(sbi, sbi);
    }
  }

  @Override
  public void onReceiveRandomWalk(Message messageObj) {
    RestartableModelMessage message = (RestartableModelMessage)messageObj;
    if (message.getTargetPid() == currentProtocolID) {
      MultiLearningModel receivedMultiLearningModel = message.getModel();
      InstanceHolder instances = ((DimensionReductionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        receivedMultiLearningModel.update(x, y);
      }
      //receivedMultiLearningModel = receivedMultiLearningModel.merge(latestMultiLearningModel);
      boolean sendOutCondition = true;
      if (sendOutCondition) {
        // stores the updated model
        responsibilityForFeedback.put(receivedMultiLearningModel.getModelInfo(),message.getSource());
        //addToModelObserver(receivedMultiLearningModel.getModelID());
        proxySendModel(receivedMultiLearningModel);
      } else {
        //removeModelByCurrentNodeFromModelObserver(receivedMultiLearningModel.getModelID());
        receivedMultiLearningModel.removeModel(); // random walk is shut down
        sendFeedback(message.getSource(), receivedMultiLearningModel.getModelInfo());
      }
    }
  }
  /*
  private void removeModelFromModelObserver(Node lastOwner, Integer modelID) {
    if(modelObserverMap.containsKey(lastOwner)){
      modelObserverMap.get(lastOwner).remove(modelID);
    }
  }
  private void removeModelByCurrentNodeFromModelObserver(Integer modelID) {
    removeModelFromModelObserver(this.currentNode,modelID);
  }

  private void addToModelObserver(Integer modelID) {
    if(modelObserverMap.containsKey(this.currentNode)){
      modelObserverMap.get(this.currentNode).add(modelID);
    } else {
      HashSet<Integer> addingHashSet = new HashSet<Integer>();
      addingHashSet.add(modelID);
      modelObserverMap.put(this.currentNode,addingHashSet);
    }
  }
   */


  @Override
  public void nextCycle(Node currentNode, int currentProtocolID) {
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;

    if( isAnyOnlineNeighbor() ){
      //init steps
      if (!isFirstInited){
        resetAllContainer();
        isFirstInited = true;
      }
      if (isFirstSessionOnline) {
        nextProxySendModelFromTheQueue();
        isFirstSessionOnline = false;
      }

      //connection timeout
      removeNodeID.clear();
      for (Long nodeID : connectionTimeOuts.keySet()) {
        if(connectionTimeOuts.get(nodeID)<CommonState.getTime()){
          connectionTimeout(connectionTimeOutsToNode.get(nodeID));
          removeNodeID.add(nodeID);
        } 
      }
      for (Long nodeID : removeNodeID) {
        connectionTimeOuts.remove(nodeID);
        connectionTimeOutsToNode.remove(nodeID);
      }
      removeNodeID.clear();
      
      //broadcast method
      sendBroadcastInfoSet.clear();
      for ( SendBroadcastInfo sbi : sendBroadcastInfoSetForNextCycle.values()) {
        sbi.nextSend();
        SendBroadcastInfo sbiForPut = (SendBroadcastInfo)sbi.clone();
        sendBroadcastInfoSet.put(sbiForPut, sbiForPut);
      }
      Node receiverNode = getRandomNeighbor();
      //push
      MultiWalkerProtocol receiver = (MultiWalkerProtocol)receiverNode.getProtocol(this.currentProtocolID);
      MultiWalkerProtocol sender = this;
      sendBroadcastInfo(receiver, sender, true);
      //pull
      receiver = this;
      sender = (MultiWalkerProtocol)receiverNode.getProtocol(this.currentProtocolID);
      sendBroadcastInfo(receiver, sender, false);

      //level2 restart check 
      if (!restartCandidateModelSet.isEmpty()){
        removeNodeID.clear();
        for (Long eventId : restartCandidateModelSet.keySet()) {
          MultiLearningModel restartCandidateModel = restartCandidateModelSet.get(eventId);
          ModelInfo restartNeededMi = restartCandidateModelInfoSet.get(eventId);
          double diffFromMaxStep = restartNeededMi.getStep()-restartCandidateModel.getStep();
          long deltaTime = CommonState.getTime() - restartNeededMi.getLastSeenTimeStamp();
          int restartDiff= (int)(deltaTime/TIMEWINDOW_FOR_RESTART);
          if (diffFromMaxStep==restartDiff){
            System.err.println("Most level 2 restart OCCURED " + CommonState.getTime() + " " + this.currentNode.getID() + " " + restartNeededMi.getModelID() + " " +
                restartNeededMi.getStep()+" "+restartCandidateModel.getStep()+" " + restartNeededMi.getWalkID() + " "+ restartCandidateModel.getWalkID()  + " "  +deltaTime+" "+eventId);
            removeNodeID.add(eventId);
            restartNeededMi.removeModel();
            restartCandidateModel.restart();
            //addToModelObserver(restartCandidateModel.getModelID());
            SendBroadcastInfo sbi = new SendBroadcastInfo(restartCandidateModel.getModelInfo(), eventId ,EventWithModelInfoEnum.RestartOccured);
            sendBroadcastInfoSetForNextCycle.remove(sbi);
            sendBroadcastInfoSetForNextCycle.put(sbi, sbi);
            sendBroadcastInfoSet.remove(sbi);
            sendBroadcastInfoSet.put(sbi, sbi);     
            sendSuperviseRequestToRandomNodeInLevel2(restartCandidateModel.getModelInfo());
            proxySendModel(restartCandidateModel);
            numberOfRestartInLevel2++;
          }
        }
        restartCandidateModelSet.keySet().removeAll(removeNodeID);
        restartCandidateModelInfoSet.keySet().removeAll(removeNodeID);
        removeNodeID.clear();
      }
    }
  }

  private void connectionTimeout(Node offlineNode){
    Long offlineNodeID = offlineNode.getID();
    boolean level1restart = this.superviseNodeAndThePotentialRestartModel.containsKey(offlineNodeID);
    boolean level2restart = superviseNodeLevel2.containsKey(offlineNodeID);

    if(sendInfo.isSending()) {
      if(sendInfo.getDest().getID() == offlineNodeID) {
        sendInfo.stopSending();
        proxySendModel(sendInfo.getModelToBeSent());
      }
    }

    removeModelInfos.clear();
    if (responsibilityForFeedback2Level.containsValue(offlineNode)){
      for ( ModelInfo modelInfo : responsibilityForFeedback2Level.keySet()) {
        if(responsibilityForFeedback2Level.get(modelInfo).getID() == offlineNodeID){
          removeModelInfos.add(modelInfo);
        }
      }
    }

    responsibilityForFeedback2Level.keySet().removeAll(removeModelInfos);
    for (ModelInfo modelInfo : removeModelInfos) {
      sendSuperviseRequestToRandomNodeInLevel2(modelInfo);
    }

    removeModelInfos.clear();
    if (responsibilityForFeedback.containsValue(offlineNode)) {
      for ( ModelInfo modelInfo : responsibilityForFeedback.keySet()) {
        if(responsibilityForFeedback.get(modelInfo).getID() == offlineNodeID){
          removeModelInfos.add(modelInfo);
          sendSuperviseRequestToRandomNodeInLevel2(modelInfo);
        }
      }
    }
    responsibilityForFeedback.keySet().removeAll(removeModelInfos);

    if ( level2restart ){
      HashSet<ModelInfo> miSet = superviseNodeLevel2.getContent(offlineNodeID);
      for (ModelInfo mi : miSet) {
        //removeModelFromModelObserver(message.getSrc(),mi.getModelID());
        mi.setThisTime();
        SendBroadcastInfo sbi = new SendBroadcastInfo(mi, EventWithModelInfoEnum.RestartNeeded);
        numberOfRestartAttemptInLevel2++;
        System.err.println("Most level 2 restart BROADCAST START " + CommonState.getTime() + " " + this.currentNode.getID() + " " + mi.getModelID()+ " "+mi.getStep() +" "+sbi.getEventID());
        sendBroadcastInfoSetForNextCycle.put(sbi, sbi);
        if (storageQueue.contains(mi)) {
          restartCandidateModelSet.put(sbi.getEventID(), storageQueue.getModel(mi));
          restartCandidateModelInfoSet.put(sbi.getEventID(), mi);
        }
      }
      superviseNodeLevel2.remove(offlineNodeID);
    }


    if (level1restart){
      HashSet<MultiLearningModel> restartedModelSet = superviseNodeAndThePotentialRestartModel.getContent(offlineNodeID);
      for (MultiLearningModel restartedModel : restartedModelSet) {
        //removeModelFromModelObserver(message.getSrc(),restartedModel.getModelID());
        restartedModel.restart();
        //addToModelObserver(restartedModel.getModelID());
        proxySendModel(restartedModel);
        sendSuperviseRequestToRandomNodeInLevel2(restartedModel.getModelInfo());
        numberOfRestartInLevel1++;
      }
      superviseNodeAndThePotentialRestartModel.remove(offlineNodeID);
    }

  }

  private void sendBroadcastInfo(MultiWalkerProtocol receiver, MultiWalkerProtocol sender, boolean isPush) {
    if (!sender.sendBroadcastInfoSet.isEmpty()) {
      for (SendBroadcastInfo senderBroadcastInfo : sender.sendBroadcastInfoSet.keySet()) {
        if(!senderBroadcastInfo.hasNextSend()){
          sender.sendBroadcastInfoSetForNextCycle.remove(senderBroadcastInfo);
        } else {
          EventWithModelInfoEnum sendSendType = senderBroadcastInfo.getSendType();
          if(receiver.getSendBroadcastInfoSet().containsKey(senderBroadcastInfo)){
            SendBroadcastInfo receiverBroadcastInfo = receiver.getSendBroadcastInfoSet().get(senderBroadcastInfo);
            EventWithModelInfoEnum recSendType = receiverBroadcastInfo.getSendType();
            if(sendSendType == EventWithModelInfoEnum.ConcurrentModelsOccured){
              if(recSendType == EventWithModelInfoEnum.ConcurrentModelsOccured){
                if(senderBroadcastInfo.getModelInfo().getWalkID() != receiverBroadcastInfo.getModelInfo().getWalkID()) {
                  if (senderBroadcastInfo.getModelInfo().getWalkID() < receiverBroadcastInfo.getModelInfo().getWalkID()) {
                    ModelInfo surviveModelInfo = senderBroadcastInfo.getModelInfo();
                    receiver.checkTheSendList(surviveModelInfo.getModelID(), surviveModelInfo.getWalkID());
                    SendBroadcastInfo storedBroadcastInfo = (SendBroadcastInfo)senderBroadcastInfo.clone();
                    storedBroadcastInfo.getModelInfo().addNoiseToLastSeenTimeStamp();
                    receiver.sendBroadcastInfoSetForNextCycle.remove(storedBroadcastInfo);
                    receiver.sendBroadcastInfoSetForNextCycle.put(storedBroadcastInfo,storedBroadcastInfo);
                    receiver.restartCandidateModelSet.remove(storedBroadcastInfo.getEventID());
                    receiver.restartCandidateModelInfoSet.remove(storedBroadcastInfo.getEventID());
                  } else {
                    //we have the same concurrent model => do nothing 
                  }
                } else {
                  //we have the same concurrent model => do nothing 
                }
              } else {
                SendBroadcastInfo storedBroadcastInfo = (SendBroadcastInfo)senderBroadcastInfo.clone();
                storedBroadcastInfo.getModelInfo().addNoiseToLastSeenTimeStamp();
                receiver.sendBroadcastInfoSetForNextCycle.remove(storedBroadcastInfo);
                receiver.sendBroadcastInfoSetForNextCycle.put(storedBroadcastInfo,storedBroadcastInfo);
                receiver.restartCandidateModelSet.remove(storedBroadcastInfo.getEventID());
                receiver.restartCandidateModelInfoSet.remove(storedBroadcastInfo.getEventID());
                receiver.checkTheSendList(storedBroadcastInfo.getModelInfo().getModelID(), storedBroadcastInfo.getModelInfo().getWalkID());
              }
            } else if (sendSendType == EventWithModelInfoEnum.RestartOccured){
              if(recSendType == EventWithModelInfoEnum.RestartOccured){
                if(senderBroadcastInfo.getModelInfo().getWalkID() != receiverBroadcastInfo.getModelInfo().getWalkID()) {
                  ModelInfo surviveModelInfo = senderBroadcastInfo.getModelInfo().getWalkID() < receiverBroadcastInfo.getModelInfo().getWalkID() ? senderBroadcastInfo.getModelInfo() : receiverBroadcastInfo.getModelInfo();
                  SendBroadcastInfo newSBI = new SendBroadcastInfo(surviveModelInfo, senderBroadcastInfo.getEventID(), EventWithModelInfoEnum.ConcurrentModelsOccured);
                  newSBI.getModelInfo().addNoiseToLastSeenTimeStamp();
                  receiver.sendBroadcastInfoSetForNextCycle.remove(newSBI);
                  receiver.sendBroadcastInfoSetForNextCycle.put(newSBI,newSBI);
                  receiver.restartCandidateModelSet.remove(newSBI.getEventID());
                  receiver.restartCandidateModelInfoSet.remove(newSBI.getEventID());
                  receiver.checkTheSendList(surviveModelInfo.getModelID(), surviveModelInfo.getWalkID());

                } else {
                  //we have the same RestartOccured => do nothing 
                }
              } else if(recSendType == EventWithModelInfoEnum.RestartNeeded) {
                SendBroadcastInfo storedBroadcastInfo = (SendBroadcastInfo)senderBroadcastInfo.clone();
                storedBroadcastInfo.getModelInfo().addNoiseToLastSeenTimeStamp();
                receiver.sendBroadcastInfoSetForNextCycle.remove(storedBroadcastInfo);
                receiver.sendBroadcastInfoSetForNextCycle.put(storedBroadcastInfo,storedBroadcastInfo);
                receiver.restartCandidateModelSet.remove(storedBroadcastInfo.getEventID());
                receiver.restartCandidateModelInfoSet.remove(storedBroadcastInfo.getEventID());
              } else {
                //we have stronger concurrentModel event => do nothing
              }
            } else {
              //we have stronger or the same event => do nothing
            }
          } else {
            SendBroadcastInfo storedBroadcastInfo = (SendBroadcastInfo)senderBroadcastInfo.clone();
            ModelInfo mi = storedBroadcastInfo.getModelInfo();
            mi.addNoiseToLastSeenTimeStamp();
            receiver.sendBroadcastInfoSetForNextCycle.remove(storedBroadcastInfo);
            receiver.sendBroadcastInfoSetForNextCycle.put(storedBroadcastInfo,storedBroadcastInfo);
            if(sendSendType == EventWithModelInfoEnum.RestartNeeded){
              if (receiver.storageQueue.contains(mi)) {
                receiver.restartCandidateModelSet.put(storedBroadcastInfo.getEventID(), receiver.storageQueue.getModel(mi));
                receiver.restartCandidateModelInfoSet.put(storedBroadcastInfo.getEventID(), mi);
              }
            } else {
              //System.err.println("Most RestartOccured Broadcast added "+CommonState.getTime() +" "+ receiver.sendBroadcastInfoSetForNextCycle.toString());
              receiver.restartCandidateModelSet.remove(storedBroadcastInfo.getEventID());
              receiver.restartCandidateModelInfoSet.remove(storedBroadcastInfo.getEventID());
              if(sendSendType == EventWithModelInfoEnum.ConcurrentModelsOccured){
                receiver.checkTheSendList(mi.getModelID(), mi.getWalkID());
              } 
            }
          }
        }
      }
    }
  }

  public void setFirstSessionOnline(Node currentNode, int currentProtocolID){
    setFirstSessionOnline(1, currentNode, currentProtocolID);
  }

  public void setFirstSessionOnline(int numberOfStartModels, Node currentNode, int currentProtocolID){
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    resetAllContainer();
    isFirstInited = true;
    isFirstSessionOnline = true;
    try {
      for (int i = 0; i < numberOfStartModels; i++) {
        MultiLearningModel initModel = (MultiLearningModel)Class.forName(modelName).getConstructor(String.class).newInstance(prefix);
        initModel.initModel();
        //addToModelObserver(initModel.getModelID());
        sendQueue.addLast(initModel);
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }

  private void resetAllContainer() {
    connectionTimeOuts.clear();
    connectionTimeOutsToNode.clear();
    sendQueue.clear();
    sendInfo.stopSending();
    sendBroadcastInfoSet.clear();
    sendBroadcastInfoSetForNextCycle.clear();
    restartCandidateModelSet.clear();
    restartCandidateModelInfoSet.clear();
    responsibilityForFeedback.clear();
    superviseNodeAndThePotentialRestartModel.clear();
    responsibilityForFeedback2Level.clear();
    superviseNodeLevel2.clear();
  }

  private void proxySendModel(MultiLearningModel modelToBeSent) {
    if(!sendInfo.isSending()){
      Node dest = getRandomNeighborForModelSend();
      long delay = computeDelay(modelToBeSent.getModelSize());
      sendInfo = new SendModelInfo(modelToBeSent, dest, CommonState.getTime()+delay);
      resetOffTime();
      EDSimulator.add(delay, new EventMessage(currentNode, dest, EventEnum.WakeUpAndSend), currentNode, currentProtocolID);
    } else {
      sendQueue.addLast(modelToBeSent);
    }
  }

  private void proxySendModelWithOffTimeDelay(MultiLearningModel modelToBeSent, Node dest, long delay) {
    sendInfo = new SendModelInfo(modelToBeSent, dest, CommonState.getTime()+delay);
    EDSimulator.add(delay, new EventMessage(currentNode, dest, EventEnum.WakeUpAndSend), currentNode, currentProtocolID);
    resetOffTime();
  }

  public void sendMessage(Node dest, RestartableModelMessage message) {
    ((MultiWalkerProtocol)dest.getProtocol(currentProtocolID)).processEvent(dest, 
        currentProtocolID, message);
  }

  private void sendMessage(Node dest) {
    sendMessage(dest,new RestartableModelMessage(this.currentNode, sendInfo.getModelToBeSent(), this.currentProtocolID));
    storageQueue.add(sendInfo.getModelToBeSent());
    if(!responsibilityForFeedback.isEmpty())
      sendFeedback(sendInfo.getModelToBeSent().getModelInfo());
    if(!responsibilityForFeedback2Level.isEmpty())
      sendFeedbackLevel2(sendInfo.getModelToBeSent().getModelInfo());
    responsibilityForFeedback.remove(sendInfo.getModelToBeSent().getModelInfo());
    responsibilityForFeedback2Level.remove(sendInfo.getModelToBeSent().getModelInfo());
    MultiLearningModel remainModel = this.superviseNodeAndThePotentialRestartModel.add(dest, sendInfo.getModelToBeSent().getModelID(), (MultiLearningModel)sendInfo.getModelToBeSent().clone());
    isConcurrentModelOccured(remainModel.getModelInfo(),sendInfo.getModelToBeSent().getModelInfo());
    //removeModelByCurrentNodeFromModelObserver(sendInfo.getModelToBeSent().getModelID());
    resetOffTime();
    nextProxySendModelFromTheQueue();
  }

  private void nextProxySendModelFromTheQueue() {
    sendInfo = new SendModelInfo();
    if (sendQueue.size() > 0){
      proxySendModel(sendQueue.removeFirst());
    } 
  }

  private void sendSuperviseRequestToRandomNodeInLevel2(ModelInfo modelInfoToBeSent) {
    sendSuperviseRequestInLevel2(getRandomNeighbor(),modelInfoToBeSent);
  }

  private void sendSuperviseRequestInLevel2(Node dest, ModelInfo modelInfo) {
    responsibilityForFeedback2Level.put(modelInfo,dest);
    ((MultiWalkerProtocol)dest.getProtocol(currentProtocolID)).processEvent(dest, 
        currentProtocolID, 
        new EventWithModelInfoMessage(currentNode, dest, modelInfo, EventWithModelInfoEnum.StartSupervisingMeInLevel2));
  }

  private void sendFeedback(ModelInfo mi) {
    if(responsibilityForFeedback.containsKey(mi))
      sendFeedback(responsibilityForFeedback.get(mi), mi);
  }

  private void sendFeedback(Node dest, ModelInfo mi) {
    if(dest.isUp()){
      ((MultiWalkerProtocol)dest.getProtocol(currentProtocolID)).processEvent(dest, 
          currentProtocolID, 
          new EventWithModelInfoMessage(currentNode, dest, mi, EventWithModelInfoEnum.StopSupervisingTheMessageHasBeenSent));
    } 
  }

  private void sendFeedbackLevel2(ModelInfo mi) {
    if(responsibilityForFeedback2Level.containsKey(mi))
      sendFeedbackLevel2(responsibilityForFeedback2Level.get(mi), mi);
  }

  private void sendFeedbackLevel2(Node dest, ModelInfo mi) {   
    if(dest.isUp()){
      ((MultiWalkerProtocol)dest.getProtocol(currentProtocolID)).processEvent(dest, 
          currentProtocolID, 
          new EventWithModelInfoMessage(currentNode, dest, mi, EventWithModelInfoEnum.StopSupervisingTheMessageHasBeenSentLevel2));
    } 
  }

  private long computeDelay(double modelSize) {
    return Math.round(modelSize);
  }

  protected Node getRandomNeighbor() {
    Linkable overlay = getOverlay();
    //List<Node> onlineNodes = new ArrayList<Node>();
    for (int i = 0; i < overlay.degree()*2; i++) {
      Node randomOnlineNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
      if( randomOnlineNode.isUp() &&
          randomOnlineNode.getID()!=currentNode.getID()) {
        return randomOnlineNode;
      }
    }
    return currentNode;
  }

  protected Node getRandomNeighborForModelSend() {
    Linkable overlay = getOverlay();
    //List<Node> onlineNodes = new ArrayList<Node>();
    for (int i = 0; i < overlay.degree()*numberOfMaxModelInQueue; i++) {
      Node randomOnlineNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
      if( randomOnlineNode.isUp() &&
          randomOnlineNode.getID()!=currentNode.getID() &&
          ((MultiWalkerProtocol)randomOnlineNode.getProtocol(currentProtocolID)).getNumberOfModelInSendQueue() < numberOfMaxModelInQueue) {
        return randomOnlineNode;
      }
    }
    return getRandomNeighbor();
  }

  private boolean isAnyOnlineNeighbor() {
    for (int i = 0; i < getOverlay().degree(); i++) {
      if (getOverlay().getNeighbor(i).isUp()) {
        return true;
      }
    }  
    return false;
  }

  public void checkTheSendList(int modelID, int walkID) {
    HashSet<MultiLearningModel> removeSet = new HashSet<MultiLearningModel>();
    for (MultiLearningModel multiLearningModel : sendQueue) {
      if(modelID == multiLearningModel.getModelID() &&
          walkID != multiLearningModel.getWalkID()) {
        System.err.println("Most remove a model "+ CommonState.getTime() + " "+ this.currentNode.getID() + " " + modelID + " ");
        removeSet.add(multiLearningModel);
      }
    }
    if(!removeSet.isEmpty()){
      for (MultiLearningModel multiLearningModel : removeSet) {
        if(!responsibilityForFeedback.isEmpty())
          sendFeedback(multiLearningModel.getModelInfo());
        if(!responsibilityForFeedback2Level.isEmpty())
          sendFeedbackLevel2(multiLearningModel.getModelInfo());
        responsibilityForFeedback.remove(multiLearningModel.getModelInfo());
        responsibilityForFeedback2Level.remove(multiLearningModel.getModelInfo());
        multiLearningModel.removeModel();
        sendQueue.remove(multiLearningModel);
      }
    }
    if(sendInfo.isSending()){  
      if(modelID == sendInfo.getModelToBeSent().getModelID() &&
          walkID != sendInfo.getModelToBeSent().getWalkID()) {
        if(!responsibilityForFeedback.isEmpty())
          sendFeedback(sendInfo.getModelToBeSent().getModelInfo());
        if(!responsibilityForFeedback2Level.isEmpty())
          sendFeedbackLevel2(sendInfo.getModelToBeSent().getModelInfo());
        responsibilityForFeedback.remove(sendInfo.getModelToBeSent().getModelInfo());
        responsibilityForFeedback2Level.remove(sendInfo.getModelToBeSent().getModelInfo());
        System.err.println("Most remove a model "+ CommonState.getTime() + " "+ this.currentNode.getID() + " " + modelID + " ");
        sendInfo.getModelToBeSent().removeModel();
        nextProxySendModelFromTheQueue();
      }
    }
    storageQueue.removeModelIfNotEqualWalkID(modelID, walkID);
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

  public SendModelInfo getSendInfo() {
    return sendInfo;
  }

  public LinkedList<MultiLearningModel> getSendQueue() {
    return sendQueue;
  }

  public int getNumberOfModelInSendQueue(){
    if(sendInfo.isSending()){
      return sendQueue.size()+1;
    } else {
      return sendQueue.size();
    }
  }

  public ArrayList<Double> getModelAgeFromSendQueue(){
    ArrayList<Double> retList = new ArrayList<Double>();
    if(sendInfo.isSending()){
      retList.add(sendInfo.getModelToBeSent().getStep());
    }
    for (MultiLearningModel multiLearningModel : sendQueue) {
      retList.add(multiLearningModel.getStep());
    }
    return retList;
  }

  public ModelContainer getModelContainer() {
    return storageQueue;
  }

  public HashMap<Long, MultiLearningModel> getRestartCandidateModelSet() {
    return restartCandidateModelSet;
  }

  public HashMap<Long, ModelInfo> getRestartCandidateModelInfoSet() {
    return restartCandidateModelInfoSet;
  }

  public HashMap<ModelInfo, Node> getResponsibilityForFeedback() {
    return responsibilityForFeedback;
  }

  public HashMap<ModelInfo, Node> getResponsibilityForFeedback2Level() {
    return responsibilityForFeedback2Level;
  }

  public HashMap<SendBroadcastInfo, SendBroadcastInfo> getSendBroadcastInfoSet() {
    return sendBroadcastInfoSet;
  }

  public HashMap<SendBroadcastInfo, SendBroadcastInfo> getSendBroadcastInfoSetForNextCycle() {
    return sendBroadcastInfoSetForNextCycle;
  }

  public SuperviseNodeContainer<MultiLearningModel> getSuperviseNodeAndThePotentialRestartModel() {
    return superviseNodeAndThePotentialRestartModel;
  }

  public SuperviseNodeContainer<ModelInfo> getSuperviseNodeLevel2() {
    return superviseNodeLevel2;
  }

  @Override
  public void setSessionLength(long sessionLength) {
    this.sessionLength = sessionLength;    
  }

  @Override
  public long getSessionLength() {
    return sessionLength;
  }

  @Override
  public void initSession(Node node, int protocol) {
    sessionID ++;
  }

  @Override
  public ResultAggregator getResults() {
    return resultAggregator;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {}

  public void addOffTime(long offTime) {
    this.offTime+=offTime; 
  }

  public void resetOffTime(){
    this.offTime=0L;
  }
}
