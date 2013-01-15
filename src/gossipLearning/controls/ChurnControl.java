package gossipLearning.controls;

import gossipLearning.interfaces.Churnable;
import gossipLearning.utils.LogNormalRandom;

import java.util.Map;
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
 * This control is responsible for modeling churn behavior which is provided in the following way:
 * <ul>
 * <li>First the control decreases the online session length of each node by one.</li>
 * <li>Then it turns off each node with negative or zero session length.</li>
 * <li>If the number of online nodes in the network is less than that of given in the <i>size</i> parameter of the control, 
 * it adjusts the number of online nodes by adding uniformly selected random offline nodes to the network with a lognormal
 * session length. Each of the newly added nodes is initialized by calling the init method provided by the Churnable @see p2pChurn.interfaces.Churnable 
 * interface.</li>
 * </ul>
 * 
 * @author Róbert Ormándi
 * @navassoc - - - Churnable
 */
public class ChurnControl implements Control {
  private static final String PAR_PID = "protocol";
  private final int pid;
  private static final String PAR_SIZE = "size";
  private final int size;
  public static final int INIT_SESSION_LENGTH = 0;
  
  public static final LogNormalRandom rand  = new LogNormalRandom(Configuration.getDouble("churn.mu"), Configuration.getDouble("churn.sigma"), Configuration.getLong("random.seed", System.currentTimeMillis()));
  
  public ChurnControl(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
    size = Configuration.getInt(prefix + "." + PAR_SIZE);
  }


  public boolean execute() {
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      Protocol prot = node.getProtocol(pid);
      if (prot instanceof Churnable) {
        Churnable churnableProt = (Churnable) prot;
        churnableProt.setSessionLength(churnableProt.getSessionLength() - 1);
        if (churnableProt.getSessionLength() <= 0) {
          node.setFailState(Fallible.DOWN);
        }
      } else {
        throw new RuntimeException("Protocol with PID=" + pid + " does not support modeling churn!!!");
      }
    }

    adjustNumberOfOnlineSessions(size);

    return false;
  }

  public static long getOnlineSessionLength() {
    long len = Math.round(rand.nextDouble());
    while (len == 0) {
      len = Math.round(rand.nextDouble());
    }
    return len;
  }
  
  
  private static Map<Long, Integer> id2idx = new TreeMap<Long, Integer>();
  private static Vector<Node> downNodes = new Vector<Node>();
  private static Map<Long,Long> offlineToOnline = new TreeMap<Long,Long>();
  public static void adjustNumberOfOnlineSessions(int size) {
    id2idx.clear();
    downNodes.clear();
    int onlineNodes = 0;
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      if (node.getFailState() == Fallible.DOWN || node.getFailState() == Fallible.DEAD) {
        downNodes.add(node);
        id2idx.put(node.getID(), i);
      } else {
        onlineNodes ++;
      }
    }
    offlineToOnline.clear();
    while (onlineNodes + offlineToOnline.size() < size && onlineNodes + downNodes.size() >= size) {
      long id = downNodes.get(CommonState.r.nextInt(downNodes.size())).getID();
      if (!offlineToOnline.containsKey(id)) {
        long len = getOnlineSessionLength();
        offlineToOnline.put(id, len);
      }
    }
    for (long id : offlineToOnline.keySet()) {
      Node node = Network.get(id2idx.get(id));
      node.setFailState(Fallible.OK);
    }
    for (long id : offlineToOnline.keySet()) {
      Node node = Network.get(id2idx.get(id));
      for (int j = 0; j < node.protocolSize(); j++) {
        Protocol prot = node.getProtocol(j);
        if (prot instanceof Churnable) {
          Churnable churnableProt = (Churnable) prot;
          churnableProt.setSessionLength(offlineToOnline.get(id));
          churnableProt.initSession(node, j);
        }
      }
    }

  }

}

