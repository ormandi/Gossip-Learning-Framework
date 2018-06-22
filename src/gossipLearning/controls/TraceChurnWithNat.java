package gossipLearning.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import gossipLearning.interfaces.protocols.ProtocolWithNatInfo;
import gossipLearning.utils.NatPair;
import gossipLearning.utils.NodeUserTrace;
import gossipLearning.utils.Session;
import gossipLearning.utils.UserTrace;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.core.SchedulerI;
import peersim.util.RandPermutation;

public class TraceChurnWithNat implements Control, SchedulerI {
  private static final String PROT = "protocol";
  private static final String TRACE_FILE = "traceFile";
  private static final String CONTAB_FILE = "connectionTableFile";

  protected TreeMap<Session, List<NodeUserTrace> > heap;
  public static Map<NatPair,Long> connectionTable;
  public static Set<Integer> possibleNatType;
  private final int pid;

  
  public TraceChurnWithNat(String prefix) {
    pid = Configuration.getPid(prefix + "." + PROT);
    final String fName = Configuration.getString(prefix + "." + TRACE_FILE);
    final String fNameConnectionTableFile = Configuration.getString(prefix + "." + CONTAB_FILE);
    heap = new TreeMap<Session, List<NodeUserTrace> >();
    connectionTable = new HashMap<NatPair,Long>();
    possibleNatType = new HashSet<Integer>();
    Vector<UserTrace> userTraces = new Vector<UserTrace>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(fName));
      String line;
      while ((line = br.readLine()) != null) {
        Vector<Session> sessions = new Vector<Session>();
        StringTokenizer tokens = new StringTokenizer(line);
        String username = tokens.nextToken(); //username
        tokens.nextToken(); //user row ID
        Long startDate = Long.parseLong(tokens.nextToken());
        int timeZone = Integer.parseInt(tokens.nextToken());
        tokens.nextToken(); // : colon
        Long.parseLong(tokens.nextToken()); //startDate
        Integer.parseInt(tokens.nextToken()); //timeZone
        long sum = 0;
        sum += Long.parseLong(tokens.nextToken());
        int natType = Integer.parseInt(tokens.nextToken());
        Boolean online = isOnline(natType);
        sessions.add(new Session(sum, natType));
        while (tokens.hasMoreTokens()) {
          sum += Long.parseLong(tokens.nextToken());
          natType = Integer.parseInt(tokens.nextToken());
          sessions.add(new Session(sum, natType));
        }
        userTraces.add(new UserTrace(sessions.toArray(new Session[sessions.size()]), online, username, timeZone, startDate));
      }
      br.close();
      br = new BufferedReader(new FileReader(fNameConnectionTableFile));
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(";");
        connectionTable.put(new NatPair(Integer.parseInt(fields[0]), Integer.parseInt(fields[1])), Long.parseLong(fields[2]));
        possibleNatType.add(Integer.parseInt(fields[0]));
        possibleNatType.add(Integer.parseInt(fields[1]));
      }
      br.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    RandPermutation rp=new RandPermutation(CommonState.r);
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      if (!rp.hasNext()) rp.reset(userTraces.size());
      UserTrace ut = userTraces.get(rp.next());
      node.setFailState(ut.isFirstOnline() ? Fallible.OK : Fallible.DOWN);
      NodeUserTrace tp = new NodeUserTrace(ut,node);
      if (tp.hasMoreSession())///
        insert(tp.nextSession(),tp);
    }
  }

  public boolean execute() {
    assert heap.firstKey().getLength() == CommonState.getTime();
    for (NodeUserTrace tp : heap.pollFirstEntry().getValue()) {
      Node node = tp.getNode();
      Protocol prot = node.getProtocol(pid);
      if (node.getFailState() == Fallible.DEAD)
        continue;
      if (tp.hasMoreSession()) {
        Session recentSession = tp.nextSession();
        insert(recentSession,tp);
        if(prot instanceof ProtocolWithNatInfo) {
          ProtocolWithNatInfo pwnat = (ProtocolWithNatInfo)prot;
          pwnat.connectionChanged(recentSession.getType());
        }
        node.setFailState(isOnline(recentSession.getType()) ? Fallible.OK : Fallible.DOWN);
      }
      else {
        node.setFailState(Fallible.DEAD);
      }
    }
    return false;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  void insert(Session key, NodeUserTrace value) {
    if (!heap.containsKey(key))
      heap.put(key,new LinkedList());
    heap.get(key).add(value);
  }

  public long getNext() {
    if (heap.isEmpty())
      return -1;
    assert heap.firstKey().getLength() > CommonState.getTime();
    return heap.firstKey().getLength();
  }

  public boolean afterSimulation() { return false; }

  public boolean active(long time) { throw new UnsupportedOperationException("Not implemented for efficiency."); }

  public boolean active() { throw new UnsupportedOperationException("Not implemented for efficiency."); }

  private boolean isOnline(int natType) {
    for (Integer otherPossibleType : possibleNatType) {
      NatPair pair = new NatPair(natType, otherPossibleType);
      if(connectionTable.containsKey(pair)) {
        if(connectionTable.get(pair) > -1) {
          return true;
        }
      }
    }
    return false;
  }
  
}