package gossipLearning.controls;

import java.util.HashMap;
import java.util.HashSet;

import gossipLearning.protocols.TreeBuilderProtocol;
import gossipLearning.utils.NodeWithIDCompare;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.reports.GraphObserver;
import peersim.util.RandPermutation;

public class TreeObserver extends GraphObserver  {
  private static final String PAR_PROT = "protocol";
  private static final String PAR_START = "numberOfTreesToStart";


  protected static int pid;

  public static HashMap<Integer, HashSet<NodeWithIDCompare>> treeParticipantsMapByTreeID = new HashMap<Integer, HashSet<NodeWithIDCompare>>();

  private int cycle;
  private int numberOfTreesToStart; 
  //private int numberOfNodeKilled; 

  public TreeObserver(String prefix) {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    cycle = 0;
    numberOfTreesToStart = Configuration.getInt(prefix + "." + PAR_START);
    //numberOfNodeKilled=0;
  }
  
  @Override
  public boolean execute() {
    if(cycle==0) {
      for (int i = 0; i < numberOfTreesToStart; i++) {
        startATree(getRandomNode(),getNextTreeID(0));
      }
    }
    /*Node nodeForDeletation = null;
    Integer treeIDOFDeletedNode = 0;*/
    HashSet<Integer> emptyTrees = new HashSet<Integer>();
    for (Integer treeID : treeParticipantsMapByTreeID.keySet()) {
      boolean isAnyOnline = false;
      for (NodeWithIDCompare node : treeParticipantsMapByTreeID.get(treeID)) {
        if(node.getNode().isUp()) {
          isAnyOnline = true;
          /*Float random = CommonState.r.nextFloat();
          //System.err.println("RANDOM: "+random);
          if(random>0.99f) {
            nodeForDeletation=node.getNode();
            treeIDOFDeletedNode=treeID;
          }*/
          break;
        }
      }
      if(!isAnyOnline) {
        emptyTrees.add(treeID);
      }
    }
    for (Integer treeID : emptyTrees) {
      endOfTree(treeID);
    }
    /*if(nodeForDeletation!=null) {
      String outStr = "";
      for (Integer treeID : treeParticipantsMapByTreeID.keySet()) {
        outStr+=treeID+" ";
      }
      System.out.println("!!!!!!!BELENYÚLOK!!!!!KITORLOM:"+nodeForDeletation.getID()+" "+treeIDOFDeletedNode);
      Node parentNode = ((TreeBuilderProtocol)nodeForDeletation.getProtocol(pid)).getParentNode();
      HashSet<NodeWithIDCompare> childrenNodeSetCopy =  new HashSet<NodeWithIDCompare>();
      for (NodeWithIDCompare nodeWithIDCompare : ((TreeBuilderProtocol)nodeForDeletation.getProtocol(pid)).getChildrenNodes()) {
        childrenNodeSetCopy.add(new NodeWithIDCompare(nodeWithIDCompare.getNode()));
      }
      
      if(parentNode!=null) {
        //System.out.print("parentNode_");
        EDSimulator.add(Math.round(TreeBuilderProtocol.OFFLINE_DELAY), new EventMessage(nodeForDeletation, parentNode, EventEnum.ConnectionTimeout), parentNode, pid);
        //((TreeBuilderProtocol)parentNode.getProtocol(pid)).connectionTimeout(parentNode,pid,nodeForDeletation);
      }
      if(!childrenNodeSetCopy.isEmpty()) {
        for (NodeWithIDCompare nodeWithIDCompare : childrenNodeSetCopy) {
          //System.out.print("childNode_");
          //((TreeBuilderProtocol)nodeWithIDCompare.getNode().getProtocol(pid)).connectionTimeout(nodeWithIDCompare.getNode(),pid,nodeForDeletation);
          EDSimulator.add(Math.round(TreeBuilderProtocol.OFFLINE_DELAY), new EventMessage(nodeForDeletation, nodeWithIDCompare.getNode(), EventEnum.ConnectionTimeout), nodeWithIDCompare.getNode(), pid);
        }
      }
      ((TreeBuilderProtocol)nodeForDeletation.getProtocol(pid)).forgetTree();
      unSubscribeFromTree(treeIDOFDeletedNode,nodeForDeletation);
      numberOfNodeKilled++;
    }*/
    double treeNodes = 0.0;
    double offlineNodes = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      //((TreeBuilderProtocol)Network.get(i).getProtocol(pid)).nextCycle(Network.get(i), pid);
      if(((TreeBuilderProtocol)Network.get(i).getProtocol(pid)).isTreeNode()) {
        treeNodes++;
      }
      if(!Network.get(i).isUp()) {
        offlineNodes++;
      }
    }
    int knownNodes=0;
    for (Integer treeID : treeParticipantsMapByTreeID.keySet()) {
      knownNodes+=treeParticipantsMapByTreeID.get(treeID).size();
    }
    System.err.println(treeNodes+" "+knownNodes+" "+offlineNodes+" ");
    cycle++;
    return false;
  }

  public static void startATree(Node node, Integer treeID) {
    newTreeWithRoot(treeID, node); 
    //System.out.println("OBSERVER TREE START: "+treeID+"");
    ((TreeBuilderProtocol)node.getProtocol(pid)).startATree(treeID,node,pid);
  }
  
  public static void endOfTree(Integer endTreeID) {
    //System.err.println(CommonState.getTime()+" "+endTreeID);
    if(treeParticipantsMapByTreeID.containsKey(endTreeID)) {
      removeTree(endTreeID);
      int nextID = getNextTreeID(endTreeID);
      Node nextNode = getRandomNode();
      /*StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      String outStr = "";
      for (StackTraceElement stackTraceElement : stackTraceElements) {
        outStr+=stackTraceElement.getMethodName()+" ";
      }*/
      //System.out.println("END_PREV_START_NEXT "+endTreeID+" "+nextID+" ROOT: "+nextNode.getID());
      if(nextNode != null) {
        startATree(nextNode,nextID);
      } else {
        System.err.println("LEHALT EGY FAEPETES !");
      }
    } else {
      //System.out.println("ALREADY_ENDED "+endTreeID);
    }
  }
  
  private static int getNextTreeID(Integer prevTreeID){
    Integer returnTreeID = CommonState.r.nextInt();
    while(returnTreeID == 0 || returnTreeID == prevTreeID || treeParticipantsMapByTreeID.containsKey(returnTreeID) ) {
      returnTreeID = CommonState.r.nextInt();
    }
    return returnTreeID;
  }
  
  private static Node getRandomNode(){
    RandPermutation rp = new RandPermutation(Network.size(),CommonState.r);
    Node returnNode;
    for (int i = 0; i < Network.size(); i++) {
      returnNode = Network.get(rp.get(i));
      boolean isFreeNode = ((TreeBuilderProtocol)returnNode.getProtocol(pid)).isFreeNode();
      if(returnNode.isUp() && isFreeNode) {
        return returnNode;
      }
    }
    return null;
  }

  public static void newTreeWithRoot(Integer treeID,Node node){
    treeParticipantsMapByTreeID.put(treeID, new HashSet<NodeWithIDCompare>());
    subscribeForTree(treeID, node);
  }
  
  public static void subscribeForTree(Integer treeID,Node node){
    if(treeParticipantsMapByTreeID.containsKey(treeID)) {
      treeParticipantsMapByTreeID.get(treeID).add(new NodeWithIDCompare(node));
    } else {
      newTreeWithRoot(treeID,node);
    }
  }
  
  public static void unSubscribeFromTree(Integer treeID,Node node) { 
    if(treeParticipantsMapByTreeID.containsKey(treeID)) {
      NodeWithIDCompare unsubnode = new NodeWithIDCompare(node);
      if(treeParticipantsMapByTreeID.get(treeID).contains(unsubnode)) {
        treeParticipantsMapByTreeID.get(treeID).remove(unsubnode);
      }
    }
  }
  
  private static void removeTree(Integer treeID) {
    if(treeParticipantsMapByTreeID.containsKey(treeID)) {
      HashSet<NodeWithIDCompare> treeParticipants = treeParticipantsMapByTreeID.remove(treeID);
      for (NodeWithIDCompare node : treeParticipants) {
        //System.out.println("OBSERVER FORGET_TREE - "+node.getID());
        ((TreeBuilderProtocol)node.getNode().getProtocol(pid)).forgetTree(treeID);
      }
    }
  }
  
}
