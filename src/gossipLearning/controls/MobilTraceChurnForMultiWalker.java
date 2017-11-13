package gossipLearning.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import gossipLearning.interfaces.protocols.Churnable;
import gossipLearning.messages.multiwalker.EventMessage;
import gossipLearning.protocols.MultiWalkerProtocol;
import gossipLearning.utils.EventEnum;
import gossipLearning.utils.UserTrace;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.transport.Transport;

/**
 * The execute function runs in every minute!
 * @author arppy
 */
public class MobilTraceChurnForMultiWalker implements Control {
  private static final String PROT = "protocol";
  private final int pid;

  private static final String TRACE_FILE = "traceFile";
  private final String fName;

  private static final String UNITS_IN_STEP = "unitsInStep";
  public final long unitsInStep;

  private static final String MODEL_TIMES = "modelTimes";
  public final double modelTimes;

  private static final String RATE_OF_AVG_ON_LINE_NODE= "rateOfAvgOnlineNode";
  public final double rateOfAvgOnlineNode;

  private static final String DELAY= "delay";
  public final long delay;

  protected Map<Long, UserTrace> assignedUserTrace;
  protected Vector<UserTrace> userTraces;

  public static int numberOfNextCycle = 0;

  public MobilTraceChurnForMultiWalker(String prefix) {
    pid = Configuration.getPid(prefix + "." + PROT);
    fName = Configuration.getString(prefix + "." + TRACE_FILE);
    unitsInStep = Configuration.getLong(prefix + "." + UNITS_IN_STEP);
    modelTimes = Configuration.getDouble(prefix + "." + MODEL_TIMES);
    rateOfAvgOnlineNode = Configuration.getDouble(prefix + "." + RATE_OF_AVG_ON_LINE_NODE);
    delay = Configuration.getLong(prefix + "." + DELAY);
    userTraces = new Vector<UserTrace>();
    assignedUserTrace = new TreeMap<Long, UserTrace>();
    System.out.println(delay);
    try {
      // reading session lengths
      BufferedReader br = new BufferedReader(new FileReader(fName));
      String line;
      while ((line = br.readLine()) != null) {
        if (line != null) {
          Vector<Long> sessions = new Vector<Long>();
          StringTokenizer tokens = new StringTokenizer(line);
          String username = tokens.nextToken();
          String onlineToken = tokens.nextToken();
          Boolean online = ('1' == onlineToken.charAt(0));
          Long startDate = Long.parseLong(tokens.nextToken());
          int timeZone = Integer.parseInt(tokens.nextToken());
          sessions.add(Long.parseLong(tokens.nextToken()));    
          while (tokens.hasMoreTokens()) {
            sessions.add(Long.parseLong(tokens.nextToken()));
          }
          Long[] sessArr = sessions.toArray(new Long[sessions.size()]);
          userTraces.add(new UserTrace(sessArr, online, username, timeZone, startDate));
        }
      }
      br.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    /**
     * initialize first session length;
     */
    if(delay>0L){
      for (int i = 0; i < Network.size(); i ++) {
        Node node = Network.get(i);
        Protocol prot = node.getProtocol(pid);
        if (prot instanceof Churnable) {
          Churnable churnableProt = (Churnable) prot;
          UserTrace ut;
          if (CommonState.r.nextDouble() < rateOfAvgOnlineNode) {
            node.setFailState(Fallible.OK);
            ut = new UserTrace(true);
            initSession(node);
            if (modelTimes < 1.0) {
              if(modelTimes > CommonState.r.nextDouble())
                ((MultiWalkerProtocol)node.getProtocol(pid)).setFirstSessionOnline(1,node,pid);
            } else {
              ((MultiWalkerProtocol)node.getProtocol(pid)).setFirstSessionOnline((int)modelTimes,node,pid);
            } 
          } else {
            node.setFailState(Fallible.DOWN);
            ut = new UserTrace(false);
          }
          churnableProt.setSessionLength(delay);
          assignedUserTrace.put(node.getID(), ut);
        }
      }
    } else {
      long sessionLength = 0;
      for (int i = 0; i < Network.size(); i ++) {
        Node node = Network.get(i);
        Protocol prot = node.getProtocol(pid);
        if (prot instanceof Churnable) {
          Churnable churnableProt = (Churnable) prot;
          int vi = CommonState.r.nextInt(userTraces.size());
          UserTrace ut = new UserTrace(userTraces.get(vi));
          if (ut.isFirstOnline()) {
            node.setFailState(Fallible.OK);
            initSession(node);
            ((MultiWalkerProtocol)node.getProtocol(pid)).setFirstSessionOnline(node,pid);
          } else {
            node.setFailState(Fallible.DOWN);
          }
          sessionLength = ut.nextSession();
          churnableProt.setSessionLength(sessionLength);
          assignedUserTrace.put(node.getID(), ut);
        }
      }
    }
  }


  @Override
  public boolean execute() {
    long sessionLength = 0;
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      Protocol prot = node.getProtocol(pid);
      if (prot instanceof Churnable) {
        Churnable churnableProt = (Churnable) prot;
        boolean prevstate = node.isUp();
        churnableProt.setSessionLength(churnableProt.getSessionLength() - unitsInStep);
        long offTime = 0L;
        while (churnableProt.getSessionLength() <= 0L) {
          if (node.isUp()) {
            node.setFailState(Fallible.DOWN);
          } else {
            node.setFailState(Fallible.OK);
            initSession(node);
          }
          UserTrace ut = assignedUserTrace.get(node.getID());
          if (ut.hasMoreSession()) {
            sessionLength = ut.nextSession();
            assignedUserTrace.put(node.getID(), ut);
          } else {
            ut.resetPointer();
            ut = getANewUserTraceBasedOnFirstOnline(prevstate);
            if (ut.isFirstOnline()) {
              node.setFailState(Fallible.OK);
              initSession(node);
            } else {
              node.setFailState(Fallible.DOWN);
            }
            sessionLength = ut.nextSession();
            assignedUserTrace.put(node.getID(), ut);
          }
          if(churnableProt.getSessionLength() + sessionLength <= 0L){
            if(node.isUp()==false)
              offTime += sessionLength;
          } else {
            if(node.isUp()==false) {
              offTime = 0L;
            } else if (node.isUp()==true &&  prevstate==false) {
              offTime = churnableProt.getSessionLength();
            } 
          }
          churnableProt.setSessionLength(churnableProt.getSessionLength() + sessionLength);
        }
        if (node.isUp() != prevstate) {
          if (node.isUp()){
            sendWakeUpTrigger(node, prot);
          } else {
            sendConnectionTimeoutEventToNodeWithActiveConnection(node, prot);
          } 
        }
        if (prot instanceof MultiWalkerProtocol){
          MultiWalkerProtocol smwProt = (MultiWalkerProtocol) prot;
          if(offTime!=0L)
            smwProt.addOffTime(offTime);
          if (node.isUp()==false && prevstate==true)
            smwProt.resetOffTime();
        }
      } else {
        throw new RuntimeException("Protocol with PID=" + pid + " does not support modeling churn!!!");
      }
      if (node.isUp() && prot instanceof CDProtocol) {
        ((CDProtocol)prot).nextCycle(node, pid);
      }
    }    
    numberOfNextCycle = 0;
    return false;
  }

  private UserTrace getANewUserTraceBasedOnFirstOnline(boolean prevstate) {
    int vi = CommonState.r.nextInt(userTraces.size());
    UserTrace ut = new UserTrace(userTraces.get(vi));
    while(ut.isFirstOnline() != prevstate) {
      vi = CommonState.r.nextInt(userTraces.size());
      ut = new UserTrace(userTraces.get(vi));
    }
    return ut;
  }

  /*
  private void sendConnectionTimeoutEventToAllNeighbor(Node node) {
    Linkable overlay = getOverlay(node);
    for (int i = 0; i < overlay.degree(); i++) {
      Node neighbor = overlay.getNeighbor(i);
      Protocol neighborProt = neighbor.getProtocol(pid);
      if (neighbor.isUp() && neighbor.getID() != node.getID() && neighborProt instanceof MultiWalkerProtocol){
        ((MultiWalkerProtocol)neighborProt).processEvent(neighbor, pid, new EventMessage(node,neighbor,EventEnum.ConnectionTimeout));
      }
    }
  } 
  */

  private void sendConnectionTimeoutEventToNodeWithActiveConnection(Node node, Protocol prot){
    MultiWalkerProtocol mwp = (MultiWalkerProtocol)prot;
    for (Node neighbor : mwp.getResponsibilityForFeedback().values()) {
      sendConnectionTimeoutEvent(node, neighbor);
    }
    
    for (Node neighbor : mwp.getResponsibilityForFeedback2Level().values()) {
      //System.err.println("Most level 2 restart GO OFFLINE " + CommonState.getTime() + " " + node.getID() + " " + neighbor.getID());
      sendConnectionTimeoutEvent(node, neighbor);
    }
    for (Node neighbor : mwp.getSuperviseNodeAndThePotentialRestartModel().getNodes()) {
      sendConnectionTimeoutEvent(node, neighbor);
    }
    for (Node neighbor : mwp.getSuperviseNodeLevel2().getNodes()) {
      sendConnectionTimeoutEvent(node, neighbor);
    }
  }


  private void sendConnectionTimeoutEvent(Node node, Node neighbor) {
    Protocol neighborProt = neighbor.getProtocol(pid);
    if (neighbor.isUp() && neighborProt instanceof MultiWalkerProtocol){
      ((MultiWalkerProtocol)neighborProt).processEvent(neighbor, pid, new EventMessage(node,neighbor,EventEnum.ConnectionTimeout));
    } else {
      System.err.println("Most NAGY a BAJ! Eltűnik egy model!"+ CommonState.getTime() + " " + node.getID() + " " + neighbor.getID()+"-----------------!!!!");
    }
  }
  
  private void sendWakeUpTrigger(Node node, Protocol prot) {
    if (prot instanceof MultiWalkerProtocol) {
      ((MultiWalkerProtocol)prot).processEvent(node, pid, new EventMessage(node,node,EventEnum.WakeUp));
    }
  }

  private void initSession(Node node) {
    for (int i = 0; i < node.protocolSize(); i++) {
      if (node.getProtocol(i) instanceof Churnable) {
        Churnable chp = (Churnable) node.getProtocol(i);
        chp.initSession(node, i);
      }
    }
  }

  /**
   * It is method which makes more easer of the accessing to the transport layer of the current node.
   * 
   * @return The transform layer is returned.
   */
  protected Transport getTransport(Node node) {
    return ((Transport) node.getProtocol(FastConfig.getTransport(pid)));
  }

  /**
   * This method supports the accessing of the overlay of the current node.
   * 
   * @return The overlay of the current node is returned.
   */
  protected Linkable getOverlay(Node node) {
    return (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
  }
}
