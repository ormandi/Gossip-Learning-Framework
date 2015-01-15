package gossipLearning.controls;

import gossipLearning.interfaces.protocols.Churnable;
import gossipLearning.utils.UserTrace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

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
public class MobilTraceDailyChurn implements Control {
  private static final String PROT = "protocol";
  private final int pid;
  
  private static final String TRACE_FILE = "traceFile";
  private static final String  DAY_ID = "dayid";
  
  private final String fName;
  
  private static final String UNITS_IN_STEP = "unitsInStep";
  private static final long DAY_IN_MILISECOND = 86400000L;
  private final long unitsInStep;
  
  private int day = 0; // Monday
  private long elapsedTime = 0;

  protected Map<Long, UserTrace> assignedUserTrace;
  protected UserTraceVector[] userTraces = new UserTraceVector[7];
  
  public MobilTraceDailyChurn(String prefix) {
    pid = Configuration.getPid(prefix + "." + PROT);
    fName = Configuration.getString(prefix + "." + TRACE_FILE);
    unitsInStep = Configuration.getLong(prefix + "." + UNITS_IN_STEP);
    day = Configuration.getInt(prefix + "." + DAY_ID);
    assignedUserTrace = new TreeMap<Long, UserTrace>();
    for (int i = 0; i < 7; i++) {
      userTraces[i] = new UserTraceVector();
    }
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
          UserTrace ut = new UserTrace(sessArr, online, username, timeZone, startDate);
          userTraces[ut.getDayName()].add(ut);
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
        int vi = CommonState.r.nextInt(userTraces[day].size());
        UserTrace ut = new UserTrace(userTraces[day].get(vi));
        if (ut.isFirstOnline()) {
          node.setFailState(Fallible.OK);
          initSession(node);
        } else {
          node.setFailState(Fallible.DOWN);
        }
        sessionLength = ut.nextSession();
        churnableProt.setSessionLength(sessionLength);
        assignedUserTrace.put(node.getID(), ut);
      }
    }
  }
  
  @Override
  public boolean execute() {
    long sessionLength = 0;
    elapsedTime += unitsInStep;
    if (elapsedTime >= DAY_IN_MILISECOND) {
      day++;
      elapsedTime -= DAY_IN_MILISECOND;
      if (day == 7) {
        day = 0;
      }
    }
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
            sessionLength = ut.nextSession();
            assignedUserTrace.put(node.getID(), ut);
          } else {
            ut.resetPointer();
            int vi = CommonState.r.nextInt(userTraces[day].size());
            ut = new UserTrace(userTraces[day].get(vi));
            if (ut.isFirstOnline()) {
              node.setFailState(Fallible.OK);
              initSession(node);
            } else {
              node.setFailState(Fallible.DOWN);
            }
            sessionLength = ut.nextSession();
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
  
  protected class UserTraceVector{
    private Vector<UserTrace> userTraceVector;
    public UserTraceVector() {
      this.userTraceVector = new Vector<UserTrace>();
    }
    public UserTrace get(int index) {
      return userTraceVector.get(index);
    }
    public int size() {
      return userTraceVector.size();
    }
    public void add(UserTrace userTrace) {
      userTraceVector.add(userTrace);
    }
  }
}
