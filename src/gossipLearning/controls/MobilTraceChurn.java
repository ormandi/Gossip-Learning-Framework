package gossipLearning.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import gossipLearning.interfaces.protocols.Churnable;
import gossipLearning.utils.Session;
import gossipLearning.utils.UserTrace;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * The execute function runs in every minute!
 * @author arppy
 */
public class MobilTraceChurn implements Control {
  private static final String PROT = "protocol";
  private final int pid;
  
  private static final String TRACE_FILE = "traceFile";
  private final String fName;
  
  private static final String UNITS_IN_STEP = "unitsInStep";
  public final long unitsInStep;

  protected Map<Long, UserTrace> assignedUserTrace;
  protected Vector<UserTrace> userTraces;

  public MobilTraceChurn(String prefix) {
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
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      Protocol prot = node.getProtocol(pid);
      if (prot instanceof Churnable) {
        Churnable churnableProt = (Churnable) prot;
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
      } else {
        throw new RuntimeException("Protocol with PID=" + pid + " does not support modeling churn!!!");
      }
    }
    return false;
  }
  
  private void initSession(Node node) {
    for (int i = 0; i < node.protocolSize(); i++) {
      if (node.getProtocol(i) instanceof Churnable) {
        Churnable chp = (Churnable) node.getProtocol(i);
        chp.initSession(node, i);
      }
    }
  }
  
}
