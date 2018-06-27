package gossipLearning.protocols;

import java.util.HashMap;
import java.util.HashSet;

import gossipLearning.controls.ChurnControl;
import gossipLearning.controls.TraceChurnWithNat;
import gossipLearning.controls.TreeObserver;
import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.protocols.DimensionReductionProtocol;
import gossipLearning.interfaces.protocols.HotPotatoProtocol;
import gossipLearning.interfaces.protocols.InstanceLoaderConnection;
import gossipLearning.interfaces.protocols.ProtocolWithNatInfo;
import gossipLearning.messages.Message;
import gossipLearning.messages.multiwalker.EventMessage;
import gossipLearning.messages.tree.TreeGradientEncryptionUpMessage;
import gossipLearning.messages.tree.TreeModelDownMessage;
import gossipLearning.models.DummySizedModel;
import gossipLearning.models.DummySumLearningModelWithEncryption;
import gossipLearning.utils.EventEnum;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.NodeWithIDCompare;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import peersim.util.RandPermutation;

public class TreeBuilderProtocolWithNat implements HotPotatoProtocol, Cloneable, InstanceLoaderConnection, ProtocolWithNatInfo {

  protected static final String PAR_EXTRACTORPID = "extractorProtocol";
  protected static final String PAR_ARRGNAME = "aggrName";
  protected static final String PAR_MODELNAME = "modelName";
  protected static final String PAR_EVALNAME = "evalName";
  protected static final String PAR_S = "s";
  protected static final String PAR_K = "k";
  private static final String PAR_MAX_MODEL_SIZE = "maxModelSize";
  private static final String PAR_MIN_MODEL_SIZE = "minModelSize";
  private static final String PAR_MAX_GRADIENT_SIZE = "maxGradientSize";
  private static final String PAR_MIN_GRADIENT_SIZE = "minGradientSize";
  private static final String PAR_OFFLINE_DELAY = "offlineDelay";
  private static final String PAR_MIN_LATENCY = "minLatency";
  private static final String PAR_MAX_ENCODING_TIME = "maxEncodingTime";
  private static final String PAR_MIN_ENCODING_TIME = "minEncodingTime";
  private static final String PAR_MAX_DECODING_TIME = "maxDecodingTime";
  private static final String PAR_MIN_DECODING_TIME = "minDecodingTime";

  public static double MAX_MODEL_SIZE;
  public static double MIN_MODEL_SIZE;
  public static double MAX_GRADIENT_SIZE;
  public static double MIN_GRADIENT_SIZE;
  public static double OFFLINE_DELAY;
  public static double MIN_LATENCY;
  public static double MAX_ENCODING_TIME;
  public static double MIN_ENCODING_TIME;
  public static double MAX_DECODING_TIME;
  public static double MIN_DECODING_TIME;

  protected Node parentNode;
  protected Node currentnode;
  protected int currentprotocolid;
  protected int natType;

  protected long sessionLength;
  protected int sessionID;

  protected final String prefix;
  protected final String modelName;
  protected final String aggrClassName;
  protected final String evalName;
  protected final int exrtactorProtocolID;
  protected ResultAggregator resultAggregator;

  protected Integer treeID;
  protected Integer levelInTree;
  protected HashSet<NodeWithIDCompare> childrenNodes;
  protected HashMap<NodeWithIDCompare, DummySumLearningModelWithEncryption > gradientsForDecoding;

  protected Node recSendTo;
  protected Integer binomParam;

  protected static Integer s;
  protected static Integer kBinomTreeParam;

  protected DummySizedModel model;
  protected DummySumLearningModelWithEncryption gradient;

  protected Long treeStartFromTimeStamp;
  protected Long modelSendSince;
  protected long timeForModelSend;

  protected boolean isOnSendGradient;
  protected boolean isAlreadyEncryptedGradient;
  //protected Node connectionTimeOutNode;
  protected boolean isConflictOnDecrypt;

  public TreeBuilderProtocolWithNat(String prefix) {
    MAX_MODEL_SIZE = Configuration.getDouble(prefix + "." + PAR_MAX_MODEL_SIZE);
    MIN_MODEL_SIZE = Configuration.getDouble(prefix + "." + PAR_MIN_MODEL_SIZE);
    MAX_GRADIENT_SIZE = Configuration.getDouble(prefix + "." + PAR_MAX_GRADIENT_SIZE);
    MIN_GRADIENT_SIZE = Configuration.getDouble(prefix + "." + PAR_MIN_GRADIENT_SIZE);
    OFFLINE_DELAY = Configuration.getDouble(prefix + "." + PAR_OFFLINE_DELAY);
    MIN_LATENCY = Configuration.getDouble(prefix + "." + PAR_MIN_LATENCY);
    MAX_ENCODING_TIME = Configuration.getDouble(prefix + "." + PAR_MAX_ENCODING_TIME);
    MIN_ENCODING_TIME = Configuration.getDouble(prefix + "." + PAR_MIN_ENCODING_TIME);
    MAX_DECODING_TIME = Configuration.getDouble(prefix + "." + PAR_MAX_DECODING_TIME);
    MIN_DECODING_TIME = Configuration.getDouble(prefix + "." + PAR_MIN_DECODING_TIME);
    parentNode = null;
    currentnode = null;
    natType = -2;
    currentprotocolid = -1;
    sessionLength = ChurnControl.INIT_SESSION_LENGTH;
    sessionID = 0;
    this.prefix=prefix;
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
    evalName = Configuration.getString(prefix + "." + PAR_EVALNAME);
    exrtactorProtocolID = Configuration.getPid(prefix + "." + PAR_EXTRACTORPID);
    String[] modelNames = {modelName};
    String[] evalNames = {evalName};
    try {
      resultAggregator = (ResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
    treeID = 0;
    levelInTree = 0;
    childrenNodes = new HashSet<NodeWithIDCompare>();
    gradientsForDecoding = new HashMap<NodeWithIDCompare, DummySumLearningModelWithEncryption>();
    recSendTo = null;
    binomParam = 0;
    s = Configuration.getInt(prefix + "." + PAR_S);
    kBinomTreeParam = Configuration.getInt(prefix + "." + PAR_K);
    model = new DummySizedModel(prefix);
    gradient = new DummySumLearningModelWithEncryption(prefix);
    treeStartFromTimeStamp = 0L;
    modelSendSince = 0L;
    timeForModelSend = 0L;
    isOnSendGradient = false;
    isAlreadyEncryptedGradient = false;
    //connectionTimeOutNode = null;
    isConflictOnDecrypt = false;
  }

  public TreeBuilderProtocolWithNat(TreeBuilderProtocolWithNat o) {
    parentNode = o.parentNode;
    currentnode = o.currentnode;
    currentprotocolid = o.currentprotocolid;
    natType = o.natType;
    sessionLength = o.sessionLength;
    sessionID = o.sessionID;
    prefix=o.prefix;
    modelName=o.modelName;
    aggrClassName=o.aggrClassName;
    evalName=o.evalName;
    exrtactorProtocolID=o.exrtactorProtocolID;
    resultAggregator = (ResultAggregator)o.resultAggregator.clone();
    treeID = o.treeID;
    levelInTree = o.levelInTree;
    childrenNodes = new HashSet<NodeWithIDCompare>();
    childrenNodes.addAll(o.childrenNodes);
    gradientsForDecoding = new HashMap<NodeWithIDCompare, DummySumLearningModelWithEncryption>();
    gradientsForDecoding.putAll(o.gradientsForDecoding);
    recSendTo = o.recSendTo;
    binomParam = o.binomParam;
    model = (DummySizedModel)o.model.clone();
    gradient = (DummySumLearningModelWithEncryption)o.gradient.clone();
    treeStartFromTimeStamp = o.treeStartFromTimeStamp;
    modelSendSince = o.modelSendSince;
    timeForModelSend = o.timeForModelSend;
    isOnSendGradient = o.isOnSendGradient;
    isAlreadyEncryptedGradient = o.isAlreadyEncryptedGradient;
    //connectionTimeOutNode = o.connectionTimeOutNode;
    isConflictOnDecrypt = o.isConflictOnDecrypt;
  }

  @Override
  public Object clone() {
    return new TreeBuilderProtocolWithNat(this);
  }

  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    this.currentnode = currentNode;
    this.currentprotocolid = currentProtocolID;
    //System.out.println("processEvent "+CommonState.getTime()+" "+currentNode.getID()+" "+treeID);
    if ((levelInTree > 1 && parentNode != null) || levelInTree == 1) {
      if (messageObj instanceof EventMessage){
        EventMessage message = (EventMessage)messageObj;
        Node dest = message.getDest();
        if (message.getEvent() == EventEnum.WakeUpAndSendModel){ 
          if(recSendTo != null) {
            ////System.err.println("SendNOW "+CommonState.getTime()+" "+currentnode.getID()+" "+treeID+" "+recSendTo.getID()+" "+recSendTo.isUp()+" "+dest.getID()+" "+dest.isUp()+" "+(CommonState.getTime()-modelSendSince <= timeForModelSend));
            if(dest.getID() == recSendTo.getID()) {
              if(CommonState.getTime()-modelSendSince <= timeForModelSend) {
                sendMessage(dest, new TreeModelDownMessage(this.currentnode,model,levelInTree,binomParam,treeID,this.currentprotocolid));
                binomParam--;
                if(binomParam>0) {
                  proxySendModelMessage();
                } else {
                  if(isAlreadyEncryptedGradient) {
                    stopModelSendAndProxySendAndEncryptLastGradientMessage();
                  }
                }
              } else {
                //System.err.println("UNSUBSCRIBE_NOTIME_TOSEND "+currentnode.getID()+" "+treeID+" "+dest.getID());
                ((TreeBuilderProtocolWithNat)dest.getProtocol(currentprotocolid)).unSubscribeFromTree(treeID); 
                binomParam=0;
                if(isAlreadyEncryptedGradient) {
                  stopModelSendAndProxySendAndEncryptLastGradientMessage();
                }
              }
            }
          }
        } else if(message.getEvent() == EventEnum.WakeUpAndResendModel) {
          proxySendModelMessage();
        } else if(message.getEvent() == EventEnum.WakeUpAndSendGradient) {
          //System.err.println("GRADIENT_SEND_END "+currentNode.getID()+" "+treeID+" "+parentNode.getID()+" "+levelInTree);
          if(parentNode != null) {
            if(parentNode.getID()==dest.getID()) {
              isOnSendGradient = false;
              TreeGradientEncryptionUpMessage newMessage = new TreeGradientEncryptionUpMessage(this.currentnode, gradient, this.currentprotocolid);
              unSubscribeFromTree(treeID);
              sendMessage(message.getDest(), newMessage);
            }
          }
        } else if(message.getEvent() == EventEnum.ConnectionTimeout) {
          connectionTimeout(this.currentnode, this.currentprotocolid, message.getSrc());
        } else if(message.getEvent() == EventEnum.WakeUpAndFinishEncrypt) {
          ////System.err.println("GRADIENT_ENCRYPTED "+CommonState.getTime()+" "+" "+currentNode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+isOnSendGradient+" "+childrenNodes.size());
          isAlreadyEncryptedGradient = true;
          if(binomParam==0 && isNotAnyFurtherChild() && !isOnSendGradient  && !isConflictOnDecrypt) {
            ////System.err.println("LEAFE_NODE"+currentNode.getID()+" "+treeID+" "+parentNode.getID());
            childrenNodes.clear();
            stopModelSendAndProxySendAndEncryptLastGradientMessage();
          }
        } else if(message.getEvent() == EventEnum.WakeUpAndFinishDecrypt) {
          NodeWithIDCompare nodeIncomingGradient = null;
          /*String outStr = "Children: ";
          for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
            outStr+=nodeWithIDCompare.getNode().getID()+" ";
          }
          outStr+= " Gradients: ";*/
          for (NodeWithIDCompare nodeWithIDCompare : gradientsForDecoding.keySet()) {
            if(childrenNodes.contains(nodeWithIDCompare)) {
              nodeIncomingGradient = nodeWithIDCompare;
              break;
            }
          }
          ////System.err.println("DECRYPT WAKEUP!!!! "+currentnode.getID()+" "+childrenNodes.size()+" "+gradientsForDecoding.size()+" "+outStr);
          if(nodeIncomingGradient != null) {
            DummySumLearningModelWithEncryption mergeGradient = gradientsForDecoding.remove(nodeIncomingGradient);
            childrenNodes.remove(nodeIncomingGradient);
            ////System.err.println("DECRYPT FELOLDAS2!!!! "+currentnode.getID()+" "+gradientsForDecoding.size());
            if(gradientsForDecoding.size() > 0) {
              ////System.err.println("DECRYPT UTKOZESS!!!! "+currentnode.getID()+" "+gradientsForDecoding.size());
              isConflictOnDecrypt=true;
              EDSimulator.add(Math.round(gradient.getModelSize()), new EventMessage(this.currentnode, this.currentnode, EventEnum.WakeUpAndFinishDecrypt),  this.currentnode, this.currentprotocolid);
            } else {
              isConflictOnDecrypt=false;
            }
            mergeAndSendGradient(mergeGradient);
          } else {
            ////System.err.println("VALAMI NEM STIMMEL");
            isConflictOnDecrypt=false;
            if(isNotAnyFurtherChild() && !isOnSendGradient) {
              proxySendAndEncryptLastGradientMessage();
            }
          }
        }
      } else if (messageObj instanceof TreeModelDownMessage) {
        ////System.err.println("GETMODEL "+CommonState.getTime()+" "+treeID+" "+currentNode);
        onReceiveModel(this.currentnode, this.currentprotocolid, (TreeModelDownMessage)messageObj);
      } else if (messageObj instanceof TreeGradientEncryptionUpMessage) {
        onReceiveGradient((TreeGradientEncryptionUpMessage)messageObj);
      }
    } else {
      //System.err.println("ERROR quitFromTree "+currentnode.getID()+" "+treeID);
      quitFromTree(this.currentnode,this.currentprotocolid);    
    }

  }

  @Override
  public void onReceiveRandomWalk(Message message) {}

  private void onReceiveModel(Node currentNode, int currentProtocolID, TreeModelDownMessage message) {
    this.currentnode=currentNode;
    this.currentprotocolid=currentProtocolID;
    setTreeInfo(this.currentnode,this.currentprotocolid,message.getSrc(),message.getTreeID(),message.getParentLevel(), message.getParentBinomParam());
    //System.err.println("MODEL_RECEIVED "+CommonState.getTime()+" "+" "+currentNode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+message.getSrc().getID());
    modelSendSince = CommonState.getTime();
    model = message.getModel(); 
    isOnSendGradient = false;
    isAlreadyEncryptedGradient = false;
    gradient = new DummySumLearningModelWithEncryption(prefix);
    gradient.init(MIN_GRADIENT_SIZE,MAX_GRADIENT_SIZE,MIN_ENCODING_TIME,MAX_ENCODING_TIME,MIN_DECODING_TIME,MAX_DECODING_TIME);
    InstanceHolder instances = ((DimensionReductionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
      // we use each samples for updating the currently processed model
      SparseVector x = instances.getInstance(sampleID);
      double y = instances.getLabel(sampleID);
      gradient.update(x, y);
    }
    EDSimulator.add(Math.round(gradient.getEncodingTime()), new EventMessage(this.currentnode, this.currentnode, EventEnum.WakeUpAndFinishEncrypt),  this.currentnode, this.currentprotocolid);
    if(binomParam>0) {
      proxySendModelMessage();
    } 
  }


  private void onReceiveGradient(TreeGradientEncryptionUpMessage message) {
    if(!isConflictOnDecrypt) {
      NodeWithIDCompare src = new NodeWithIDCompare(message.getSrc());
      if(childrenNodes.contains(src)) {
        childrenNodes.remove(src);
        gradientsForDecoding.remove(src);
        if(gradientsForDecoding.size() > 0) {
          /*String outStr = "DECODE_FROM: "+message.getSrc().getID()+" ";
          for (NodeWithIDCompare nodeWithIDCompare : gradientsForDecoding.keySet()) {
            outStr+=nodeWithIDCompare.getNode().getID()+" ";
          }
          outStr+="CHILDREN: "+message.getSrc().getID()+" ";
          for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
            outStr+=nodeWithIDCompare.getNode().getID()+" ";
          }*/
          ////System.err.println("DECRYPT UTKOZESS!!!! "+currentnode.getID());//+" "+outStr);
          isConflictOnDecrypt=true;
          EDSimulator.add(Math.round(gradient.getModelSize()), new EventMessage(this.currentnode, this.currentnode, EventEnum.WakeUpAndFinishDecrypt),  this.currentnode, this.currentprotocolid);
        } else {
          isConflictOnDecrypt=false;
        }
        mergeAndSendGradient(message.getModel());
      }
    }
  }

  private void mergeAndSendGradient(DummySumLearningModelWithEncryption merge) {
    gradient=gradient.merge(merge);
    //String outStr = "Children:";
    //for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
    //    outStr+=" "+nodeWithIDCompare.getNode().getID();
    //}
    ////System.err.println("GRADIENT_RECEIVED_AND_MERGED "+currentnode.getID()+" "+treeID+" "+gradient.getSum()+" "+binomParam+" "+levelInTree+" "+isOnSendGradient+" "+childrenNodes.size()+" "+outStr);
    if(levelInTree > 1) {
      if(isNotAnyFurtherChild() && !isOnSendGradient && !isConflictOnDecrypt) {
        proxySendAndEncryptLastGradientMessage();
      }
    } else {
      endOfTree(treeID,gradient.getSum());
    }
  }
  /*
  @Override
  public void nextCycle(Node currentNode, int currentProtocolID) {
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    if(connectionTimeOutNode != null) {
      connectionTimeout(this.currentNode, this.currentProtocolID, connectionTimeOutNode);
      connectionTimeOutNode = null;
    }
  }
   */

  public void startATree(Integer treeID, Node currentNode, int currentProtocolID) {
    this.treeID=treeID;
    this.currentprotocolid = currentProtocolID;
    this.currentnode = currentNode;
    TreeObserver.newTreeWithRoot(treeID, currentNode);
    treeStartFromTimeStamp = CommonState.getTime();
    model = new DummySizedModel(prefix);
    model.init(MIN_MODEL_SIZE,MAX_MODEL_SIZE);
    ////System.err.println(CommonState.getTime()+" TREE_START: "+treeID+" ROOT: "+currentNode.getID());
    onReceiveModel(this.currentnode,this.currentprotocolid,new TreeModelDownMessage(currentNode, model, 0, 1, treeID, currentProtocolID));
  }

  public void endOfTree(Integer treeID, Integer result) {
    if(this.treeID!=0 && treeID==this.treeID) {
      long timeDif = CommonState.getTime()-treeStartFromTimeStamp;
      //String outStr ="";
      //StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      //for (StackTraceElement stackTraceElement : stackTraceElements) {
      //  outStr+=stackTraceElement.getMethodName()+" ";
      //}
      //for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
      //  outStr+=nodeWithIDCompare.getNode().isUp()+" "+((TreeBuilderProtocol)nodeWithIDCompare.getNode().getProtocol(currentprotocolid)).treeID+" ";
      //}
      System.out.println((treeStartFromTimeStamp/Network.size())+" "+result+" "+(timeDif/Network.size()));//+" "+treeID+" "+outStr);//+" "+treeID+" "+childrenNodes.size()+" "+outStr);
      unSubscribeFromTree(treeID);
      TreeObserver.endOfTree(treeID);
    }
  }

  public void wakeUpAndForgetEverything(Node currentNode, int currentProtocolID){
    this.currentprotocolid = currentProtocolID;
    this.currentnode = currentNode;
    ////System.err.println("wakeUpAndForgetEverything "+CommonState.getTime()+" "+currentnode.getID()+" "+treeID);
    unSubscribeFromTree(treeID); 
  }
  
  public void quitFromTree(Node currentNode, int currentProtocolID) {
    //System.err.print("quitFromTree "+CommonState.getTime()+" "+currentNode.getID()+" "+treeID);
    this.currentnode = currentNode;
    this.currentprotocolid = currentProtocolID;
    if(levelInTree == 1) {
      endOfTree(treeID,gradient.getSum());
    } else {
      if(parentNode != null) { //send connection timeout to my parent
        //System.err.print(" Parent: "+parentNode.getID());
        EDSimulator.add(Math.round(OFFLINE_DELAY), new EventMessage(this.currentnode, parentNode, EventEnum.ConnectionTimeout), parentNode, this.currentprotocolid);
        if(isOnSendGradient) {
          ((TreeBuilderProtocolWithNat)parentNode.getProtocol(currentProtocolID)).removeGradientFromQueue(new NodeWithIDCompare(this.currentnode));
        }
        //((TreeBuilderProtocol)parentNode.getProtocol(currentProtocolID)).connectionTimeOutNode = this.currentNode;
      }
      if(!childrenNodes.isEmpty()) { //send connection timeout to my children
        //System.err.print(" Children: ");
        for (NodeWithIDCompare child : childrenNodes) {
          //System.out.print("childNode_");
          //((TreeBuilderProtocol)nodeWithIDCompare.getNode().getProtocol(pid)).connectionTimeout(nodeWithIDCompare.getNode(),pid,nodeForDeletation);
          //System.err.print(child.getNode().getID()+" ");
          EDSimulator.add(Math.round(OFFLINE_DELAY), new EventMessage(this.currentnode, child.getNode(), EventEnum.ConnectionTimeout), child.getNode(), this.currentprotocolid);
        }
      }
      unSubscribeFromTree(treeID);   
    }
    //System.err.println();
  }


  private void stopModelSendAndProxySendAndEncryptLastGradientMessage(){
    recSendTo=null;
    timeForModelSend=0L;
    modelSendSince=0L;
    /////System.err.println("STOPMODELSEND"+CommonState.getTime()+" "+" "+currentnode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+isOnSendGradient+" "+childrenNodes.size());
    if(isNotAnyFurtherChild() && !isOnSendGradient  && !isConflictOnDecrypt) {
      proxySendAndEncryptLastGradientMessage();
    }
  }



  private void proxySendAndEncryptLastGradientMessage(){
    isOnSendGradient = true;
    long delay = Math.round(gradient.getModelSize());
    boolean isNewGradientSend = ((TreeBuilderProtocolWithNat)parentNode.getProtocol(currentprotocolid)).putGradientToQueue(new NodeWithIDCompare(currentnode), gradient, treeID);
    ////System.err.println("GRADIENT_SEND_START "+parentNode.getID()+" "+CommonState.getTime()+" "+" "+currentnode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+isOnSendGradient+" "+childrenNodes.size()+" "+isNewGradientSend);
    if(isNewGradientSend) {
      EDSimulator.add(delay, new EventMessage(currentnode, parentNode, EventEnum.WakeUpAndSendGradient), currentnode, currentprotocolid);
    } else {
      quitFromTree(currentnode, currentprotocolid);
    }
  }

  private void proxySendModelMessage(){
    Node dest = getRandomNeighbor();
    long delay =  Math.round(model.getModelSize());
    long predDurationOfModelSendTime = (CommonState.getTime()+delay)-modelSendSince;
    //System.out.println("MODEL_SEND_START "+" "+currentNode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+dest.getID());
    //System.err.println("MODEL_SEND_START "+" "+currentnode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+dest.getID()+" "+predDurationOfModelSendTime+" "+timeForModelSend);
    if(predDurationOfModelSendTime <= timeForModelSend) {
      if(dest.getID() != currentnode.getID()) {
        childrenNodes.add(new NodeWithIDCompare(dest));
        recSendTo=dest;
        ((TreeBuilderProtocolWithNat)dest.getProtocol(currentprotocolid)).setTreeInfo(dest,currentprotocolid,currentnode,treeID,levelInTree,binomParam);
        EDSimulator.add(delay, new EventMessage(currentnode, dest, EventEnum.WakeUpAndSendModel), currentnode, currentprotocolid);
        //System.out.println("MODEL_SEND_START2 "+" "+currentnode.getID()+" "+treeID+" "+((TreeBuilderProtocol)dest.getProtocol(currentprotocolid)).treeID);
      } else {
        EDSimulator.add(delay, new EventMessage(currentnode, currentnode, EventEnum.WakeUpAndResendModel), currentnode, currentprotocolid);
      }
    } else {
      binomParam=0;
      if(isAlreadyEncryptedGradient) {
        stopModelSendAndProxySendAndEncryptLastGradientMessage();
      }
    }
  }

  @Override
  public void connectionChanged(int newNatType) {
    if ( TraceChurnWithNat.isOnline(this.natType) && TraceChurnWithNat.isOnline(newNatType) ) {
      quitFromTree(currentnode, currentprotocolid);
      wakeUpAndForgetEverything(currentnode, currentprotocolid);
    } else if ( !TraceChurnWithNat.isOnline(this.natType) && TraceChurnWithNat.isOnline(newNatType) ) {
      wakeUpAndForgetEverything(currentnode, currentprotocolid);
    } else if ( TraceChurnWithNat.isOnline(this.natType) && !TraceChurnWithNat.isOnline(newNatType) ) {
      quitFromTree(currentnode, currentprotocolid);
    } 
    this.natType = newNatType;
  }

  @Override
  public int getNatType() {
    return this.natType;
  }
  
  
  public void connectionTimeout(Node currentNode, int currentProtocolID, Node offlineNode){
    this.currentnode = currentNode;
    this.currentprotocolid = currentProtocolID;
    //System.err.println("connectionTimeout "+CommonState.getTime()+" "+currentNode.getID()+" "+treeID+" "+offlineNode.getID());
    if(levelInTree == 1) {
      endOfTree(treeID,gradient.getSum());
    } else {
      NodeWithIDCompare offlinenode= new NodeWithIDCompare(offlineNode);
      //System.out.println("connectionTimeout"+currentNode.getID()+" "+treeID+" "+binomParam+" "+levelInTree+" "+offlineNode.getID());
      if(parentNode != null) {
        if(parentNode.getID() == offlineNode.getID()) { // my parent has gone
          //if parentNode==recSendTo then nothing special needs to be done, just forget everything 
          if(levelInTree == 2) {
            ((TreeBuilderProtocolWithNat)parentNode.getProtocol(this.currentprotocolid)).endOfTree(treeID,0);
            return;
          }
          if(!childrenNodes.isEmpty()) {
            for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
              //System.out.print("childNode_"+nodeWithIDCompare.getNode().getID());
              //((TreeBuilderProtocol)nodeWithIDCompare.getNode().getProtocol(currentProtocolID)).connectionTimeOutNode = this.currentNode;
              EDSimulator.add(Math.round(MIN_LATENCY), new EventMessage(this.currentnode, nodeWithIDCompare.getNode(), EventEnum.ConnectionTimeout), nodeWithIDCompare.getNode(), this.currentprotocolid);
            }
          }
          unSubscribeFromTree(treeID);
          return;
        }
      }
      if(!childrenNodes.isEmpty()) {
        if (childrenNodes.contains(offlinenode)) { // my child has gone
          childrenNodes.remove(offlinenode);
          //System.out.println(isNotAnyFurtherChild() +" "+(recSendTo != null) );
          if(recSendTo != null) {
            if(recSendTo.getID() == offlineNode.getID()) {
              recSendTo=null;
              proxySendModelMessage();
              return;
            }
          }
          removeGradientFromQueue(offlinenode);
          if(isNotAnyFurtherChild() && !isOnSendGradient  && !isConflictOnDecrypt) {
            proxySendAndEncryptLastGradientMessage();
          }
          return;
        }
      }
    }
  }

  private Node getRandomNeighbor() {
    Linkable overlay = getOverlay();
    RandPermutation rp = new RandPermutation(overlay.degree(),CommonState.r);
    for (int i = 0; i < overlay.degree(); i++) {
      //System.out.println("GET_RANDOM_NEIGHBOR - "+rp.get(i));
      Node randomOnlineNode = overlay.getNeighbor(rp.get(i));
      if( randomOnlineNode.isUp() &&
          randomOnlineNode.getID()!=currentnode.getID() &&
          ((TreeBuilderProtocolWithNat)randomOnlineNode.getProtocol(currentprotocolid)).isFreeNode()) {
        return randomOnlineNode;
      }
    }
    return currentnode;
  }

  private void sendMessage(Node dest, TreeModelDownMessage message) {
    //System.err.println("SEND_NOW "+CommonState.getTime()+" "+currentnode.getID()+" "+treeID+" "+dest.getID()+" "+dest.isUp()+" "+message.getParentLevel()+" "+
    //      (((TreeBuilderProtocol)dest.getProtocol(currentprotocolid)).parentNode==null)+" "+
    //      (((TreeBuilderProtocol)dest.getProtocol(currentprotocolid)).treeID+" "));
    ((TreeBuilderProtocolWithNat)dest.getProtocol(currentprotocolid)).processEvent(dest, currentprotocolid, message);
  }

  private void sendMessage(Node dest, TreeGradientEncryptionUpMessage message) {
    ((TreeBuilderProtocolWithNat)dest.getProtocol(currentprotocolid)).processEvent(dest, currentprotocolid, message);
  }

  private boolean isNotAnyFurtherChild() {
    if(childrenNodes.size()<=0) {
      return true;
    }
    for (NodeWithIDCompare nodeWithIDCompare : childrenNodes) {
      if(nodeWithIDCompare.getNode().isUp()) {
        return false;
      }
    }
    return true;
  }

  public boolean isTreeNode() {
    return !isFreeNode();
  }

  public boolean isFreeNode() {
    return treeID==0;
  }

  private void unSubscribeFromTree(Integer treeID){
    TreeObserver.unSubscribeFromTree(this.treeID, this.currentnode);
    forgetTree(treeID);
  }

  private void subscribeTree() {
    TreeObserver.subscribeForTree(treeID, currentnode);
  }

  public void forgetTree(Integer treeID) {
    ////System.err.println("FORGET_TREE "+CommonState.getTime()+" "+treeID+" "+this.treeID+" "+this.currentnode.getID());
    //StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    //String outStr="";
    //for (StackTraceElement stackTraceElement : stackTraceElements) {
    //  outStr+=stackTraceElement.getMethodName()+" ";
    //}
    //System.err.println("FORGET TREE "+ CommonState.getTime()+" "+ currentnode.getID() +" "+ this.treeID+" "+ treeID+" "+outStr);
    if(treeID == this.treeID) {
      parentNode = null;
      this.treeID = 0;
      levelInTree = 0;
      childrenNodes = new HashSet<NodeWithIDCompare>();
      gradientsForDecoding = new HashMap<NodeWithIDCompare, DummySumLearningModelWithEncryption>();
      recSendTo = null;
      binomParam = 0;
      treeStartFromTimeStamp = 0L;
      modelSendSince = 0L;
      timeForModelSend = 0L;
      isOnSendGradient = false;
      isAlreadyEncryptedGradient = false;
      isConflictOnDecrypt = false;
      gradient = new DummySumLearningModelWithEncryption(prefix);
      gradient.init(MIN_GRADIENT_SIZE,MAX_GRADIENT_SIZE,MIN_ENCODING_TIME,MAX_ENCODING_TIME,MIN_DECODING_TIME,MAX_DECODING_TIME);
      model = new DummySizedModel(prefix);
      model.init(MIN_MODEL_SIZE,MAX_MODEL_SIZE);
      //connectionTimeOutNode = null;
    }
  }

  public void setTreeInfo(Node currentNode, int currentProtocolID, Node parent, Integer treeID, Integer parentLevel, Integer parentBinomParam) {
    this.currentnode = currentNode;
    this.currentprotocolid = currentProtocolID;
    setParent(parent);
    setTreeID(treeID);
    setLevelAndBinomParam(parentLevel,parentBinomParam);
    timeForModelSend=Math.round(binomParam*MAX_MODEL_SIZE)+1L;
    childrenNodes = new HashSet<NodeWithIDCompare>();
    gradientsForDecoding = new HashMap<NodeWithIDCompare, DummySumLearningModelWithEncryption>();
    subscribeTree();
    //System.err.println("SETINFO "+currentNode.getID()+" "+parentNode.getID()+" "+treeID);  
  }

  private void setParent(Node node){
    parentNode = node;
  }

  private void setTreeID(Integer treeID) {
    this.treeID = treeID;
  }

  private void setLevelAndBinomParam(Integer parentLevel, Integer parentBinomParam){
    levelInTree = parentLevel+1;
    if(levelInTree == 1) {
      binomParam = 1;
    } else if(levelInTree < s) {
      binomParam = 1;
    } else if (levelInTree == s) {
      binomParam = kBinomTreeParam;
    } else if (levelInTree > s) {
      binomParam = parentBinomParam-1;
    }  
  }

  public void removeGradientFromQueue(NodeWithIDCompare child) {
    this.gradientsForDecoding.remove(child);
  }

  public boolean putGradientToQueue(NodeWithIDCompare child, DummySumLearningModelWithEncryption gradient, Integer treeID) {
    if(this.treeID == treeID) {
      if(!this.gradientsForDecoding.containsKey(child)) {
        this.gradientsForDecoding.put(child, gradient);
        return true;
      }
    }
    return false;
  }

  public Node getParentNode() {
    return parentNode;
  }

  public Integer getTreeID() {
    return treeID;
  }

  public HashSet<NodeWithIDCompare> getChildrenNodes() {
    return childrenNodes;
  }

  /**
   * It is method which makes more easer of the accessing to the transport layer of the current node.
   * 
   * @return The transform layer is returned.
   */
  protected Transport getTransport() {
    return ((Transport) currentnode.getProtocol(FastConfig.getTransport(currentprotocolid)));
  }

  /**
   * This method supports the accessing of the overlay of the current node.
   * 
   * @return The overlay of the current node is returned.
   */
  protected Linkable getOverlay() {
    return (Linkable) currentnode.getProtocol(FastConfig.getLinkable(currentprotocolid));
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
    sessionID++;
  }

  @Override
  public ResultAggregator getResults() {
    return resultAggregator;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {}


}
