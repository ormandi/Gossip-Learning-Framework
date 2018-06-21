package gossipLearning.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import gossipLearning.interfaces.protocols.Churnable;
import gossipLearning.messages.ConnectionTimeoutMessage;
import gossipLearning.messages.multiwalker.WaitingMessage;
import gossipLearning.protocols.SoloWalkerProtocol;
import gossipLearning.utils.Session;
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
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

/**
 * The execute function runs in every minute!
 * @author arppy
 */
public class MobilTraceChurnForSoloWalker implements Control {
  private static final String PROT = "protocol";
  private final int pid;
  
  private static final String TRACE_FILE = "traceFile";
  private final String fName;
  
  private static final String UNITS_IN_STEP = "unitsInStep";
  public final long unitsInStep;

  protected Map<Long, UserTrace> assignedUserTrace;
  protected Vector<UserTrace> userTraces;
  
  public static int numberOfNextCycle = 0;
  public static int numberOfOnlineNode = 0;

  public MobilTraceChurnForSoloWalker(String prefix) {
    pid = Configuration.getPid(prefix + "." + PROT);
    fName = Configuration.getString(prefix + "." + TRACE_FILE);
    unitsInStep = Configuration.getLong(prefix + "." + UNITS_IN_STEP);
    userTraces = new Vector<UserTrace>();
    assignedUserTrace = new TreeMap<Long, UserTrace>();

    try {
      // reading session lengths
      BufferedReader br = new BufferedReader(new FileReader(fName));
      String line;
      while ((line = br.readLine()) != null) {
        if (line != null) {
          Vector<Session> sessions = new Vector<Session>();
          StringTokenizer tokens = new StringTokenizer(line);
          String username = tokens.nextToken();
          String onlineToken = tokens.nextToken();
          Boolean online = ('1' == onlineToken.charAt(0));
          Long startDate = Long.parseLong(tokens.nextToken());
          int timeZone = Integer.parseInt(tokens.nextToken());
          sessions.add(new Session(Long.parseLong(tokens.nextToken()),online?1:0));
          boolean nextStatus = online;
          while (tokens.hasMoreTokens()) {
            sessions.add(new Session(Long.parseLong(tokens.nextToken()),(nextStatus?1:0)));
            nextStatus=!nextStatus;;
          }
          Session[] sessArr = sessions.toArray(new Session[sessions.size()]);
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
        } else {
          node.setFailState(Fallible.DOWN);
        }
        sessionLength = ut.nextSession().getLength();
        churnableProt.setSessionLength(sessionLength);
        assignedUserTrace.put(node.getID(), ut);
      }
    }
  }
  

  @Override
  public boolean execute() {
    
    long sessionLength = 0;
    Set<Long> changedStateIDs = new HashSet<Long>();
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      if (node.isUp()) {
        numberOfOnlineNode++;
      }
      Protocol prot = node.getProtocol(pid);
      if (prot instanceof Churnable) {
        Churnable churnableProt = (Churnable) prot;
        boolean prevstate = node.isUp();
        churnableProt.setSessionLength(churnableProt.getSessionLength() - unitsInStep);
        while (churnableProt.getSessionLength() <= 0L) {
          if (node.isUp()) {
            node.setFailState(Fallible.DOWN);
          } else {
            node.setFailState(Fallible.OK);
            initSession(node);
          }
          UserTrace ut = assignedUserTrace.get(node.getID());
          if (ut.hasMoreSession()) {
            sessionLength = ut.nextSession().getLength();
            assignedUserTrace.put(node.getID(), ut);
          } else {
            ut.resetPointer();
            int vi = CommonState.r.nextInt(userTraces.size());
            ut = new UserTrace(userTraces.get(vi));
            if (ut.isFirstOnline()) {
              node.setFailState(Fallible.OK);
              initSession(node);
            } else {
              node.setFailState(Fallible.DOWN);
            }
            sessionLength = ut.nextSession().getLength();
            assignedUserTrace.put(node.getID(), ut);
          }          
          churnableProt.setSessionLength(churnableProt.getSessionLength() + sessionLength);
        }
        if (node.isUp() != prevstate) {
          changedStateIDs.add(node.getID());
          if (node.isUp()){
            sendWakeUpWaitingMessage(node);
          } else {
            sendConnectionTimeoutMessageToAllNeighbor(node);       
          } 
        }
      } else {
        throw new RuntimeException("Protocol with PID=" + pid + " does not support modeling churn!!!");
      }
      if (node.isUp() && prot instanceof CDProtocol) {
        ((CDProtocol)prot).nextCycle(node, pid);
      }
    }
    for (Integer id : SoloWalkerProtocol.knownRandomWalks.keySet()) {
      if (changedStateIDs.contains(SoloWalkerProtocol.knownRandomWalks.get(id).getNodeID())){
        if (SoloWalkerProtocol.knownRandomWalks.get(id).isOnline()) {
          SoloWalkerProtocol.knownRandomWalks.get(id).setOnline(false);
        } else {
          SoloWalkerProtocol.knownRandomWalks.get(id).setOnline(true);
        }
      }
    }
    //System.out.print(CommonState.getTime()+" "+ MobilTraceChurnForSoloWalker.numberOfNextCycle +" " + MobilTraceChurnForSoloWalker.numberOfOnlineNode);
    numberOfNextCycle = 0;
    numberOfOnlineNode = 0;
    return false;
  }
  
  private void sendConnectionTimeoutMessageToAllNeighbor(Node node) {
    Linkable overlay = getOverlay(node);
    for (int i = 0; i < overlay.degree(); i++) {
      Node neighbor = overlay.getNeighbor(i);
      if (neighbor.isUp() && neighbor.getID() != node.getID()){
        ConnectionTimeoutMessage message = new ConnectionTimeoutMessage(node);
        getTransport(node).send(node, neighbor, message, pid);
      }
    }
  }
  
  private void sendWakeUpWaitingMessage(Node node) {
    EDSimulator.add(0L, new WaitingMessage(), node, pid);
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
