package gossipLearning.controls;

import gossipLearning.interfaces.protocols.Churnable;

import java.io.BufferedReader;
import java.io.FileReader;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * The execute function runs in every minute!
 * @author István Hegedűs
 */
public class MobilModelChurn implements Control {
  private static final String PROT = "protocol";
  private final int pid;
  
  private static final String TRACE_FILE = "traceFile";
  private final String fName;
  
  private static final String HOUR_BLOCKS = "hourBlocks";
  private final int hourBlocks;
  
  private static final String LENGTH_CLUSTS = "lengthClusts";
  private final int lengthClusts;
  
  private static final String UNITS_IN_STEP = "unitsInStep";
  private final long unitsInStep;
  
  private static final String INIT_TIME = "initTime";
  private final long initTime;
  
  protected static final double CL_LIMIT_1 = Math.exp(2.0);
  protected static final double CL_LIMIT_2 = Math.exp(5.0);
  
  // hourId X lengthClust (X defined numbers in a row)
  protected long[][][] onlineSessions;
  protected long[][][] offlineSessions;
  protected long[] prevLengths;
  protected long numExecutes;

  public MobilModelChurn(String prefix) {
    pid = Configuration.getPid(prefix + "." + PROT);
    fName = Configuration.getString(prefix + "." + TRACE_FILE);
    hourBlocks = Configuration.getInt(prefix + "." + HOUR_BLOCKS);
    lengthClusts = Configuration.getInt(prefix + "." + LENGTH_CLUSTS);
    unitsInStep = Configuration.getLong(prefix + "." + UNITS_IN_STEP);
    initTime = Configuration.getLong(prefix + "." + INIT_TIME);
    onlineSessions = new long[hourBlocks][lengthClusts][];
    offlineSessions = new long[hourBlocks][lengthClusts][];
    numExecutes = 0;
    try {
      // reading session lengths
      BufferedReader br = new BufferedReader(new FileReader(fName));
      String line;
      String[] split;
      String[] sp;
      while ((line = br.readLine()) != null) {
        split = line.split("\\s");
        sp = split[0].split("_");
        if (sp[0].equals("OK")) {
          int i = Integer.parseInt(sp[1]);
          int j = Integer.parseInt(sp[2]);
          onlineSessions[i][j] = new long[split.length - 1];
          for (int k = 0; k < onlineSessions[i][j].length; k++) {
            onlineSessions[i][j][k] = Long.parseLong(split[k + 1]);
          }
        } else {
          int i = Integer.parseInt(sp[1]);
          int j = Integer.parseInt(sp[2]);
          offlineSessions[i][j] = new long[split.length - 1];
          for (int k = 0; k < offlineSessions[i][j].length; k++) {
            offlineSessions[i][j][k] = Long.parseLong(split[k + 1]);
          }
        }
      }
      br.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public boolean execute() {
    if (prevLengths == null) {
      prevLengths = new long[Network.size()];
      // initialize randomly from the three categories
      for (int i = 0; i < Network.size(); i++) {
        int r = CommonState.r.nextInt(3);
        if (r == 0) {
          prevLengths[i] = 0;
        } else if (r == 1) {
          prevLengths[i] = (long)CL_LIMIT_1 + 1;
        } else {
          prevLengths[i] = (long)CL_LIMIT_2 + 1;
        }
      }
    }
    
    // determines the hourId
    int hourId = (int)((numExecutes / 60) / (24 / hourBlocks)) % hourBlocks;
    numExecutes ++;
    
    for (int i = 0; i < Network.size(); i ++) {
      Node node = Network.get(i);
      Protocol prot = node.getProtocol(pid);
      if (prot instanceof Churnable) {
        Churnable churnableProt = (Churnable) prot;
        
        // update session length of node
        churnableProt.setSessionLength(churnableProt.getSessionLength() - unitsInStep);
        
        // node reaches the end of the session
        while (churnableProt.getSessionLength() <= 0) {
          
          // determines the session length cluster
          int clId = 2;
          if (prevLengths[i] < CL_LIMIT_1) {
            clId = 0;
          } else if (prevLengths[i] < CL_LIMIT_2) {
            clId = 1;
          }
          
          // changes the failstate and sets the new session length
          long sLength = 0;
          if (node.getFailState() == Fallible.OK) {
            node.setFailState(Fallible.DOWN);
            sLength = getSessionLength(hourId, clId, false);
          } else if (node.getFailState() == Fallible.DOWN) {
            node.setFailState(Fallible.OK);
            sLength = getSessionLength(hourId, clId, true);
            if (numExecutes >= initTime) {
              churnableProt.initSession(node, pid);
            }
          }
          prevLengths[i] = sLength;
          churnableProt.setSessionLength(churnableProt.getSessionLength() + sLength);
        }
      } else {
        throw new RuntimeException("Protocol with PID=" + pid + " does not support modeling churn!!!");
      }
    }

    return false;
  }
  
  protected long getSessionLength(int hourId, int clustId, boolean isOnline) {
    if (isOnline) {
      int rand = CommonState.r.nextInt(onlineSessions[hourId][clustId].length);
      return onlineSessions[hourId][clustId][rand];
    } else {
      int rand = CommonState.r.nextInt(offlineSessions[hourId][clustId].length);
      return offlineSessions[hourId][clustId][rand];
    }
  }

}
