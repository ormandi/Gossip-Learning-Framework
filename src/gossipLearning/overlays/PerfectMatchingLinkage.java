package gossipLearning.overlays;

import gossipLearning.utils.Utils;

import java.io.Serializable;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class PerfectMatchingLinkage implements Linkable, Serializable, Protocol {
  private static final long serialVersionUID = -2569539648773271887L;
  private static final String PAR_SIZE = "size";

  protected static int size;
  protected static int[][] neighbors;
  protected static int[] indices;
  protected static Node[] nodes;
  
  public PerfectMatchingLinkage(String prefix) {
    size = Configuration.getInt(prefix + "." + PAR_SIZE);
    nodes = new Node[Network.size()];
    indices = new int[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      indices[i] = i;
    }
    
  }
  
  protected PerfectMatchingLinkage(PerfectMatchingLinkage a) {
  }
  
  public Object clone() {
    return new PerfectMatchingLinkage(this);
  }
  
  @Override
  public void onKill() {
  }

  @Override
  public boolean addNeighbor(Node arg0) {
    return false;
  }

  @Override
  public boolean contains(Node arg0) {
    int currentNodeID = (int)CommonState.getNode().getID();
    for (int i = 0; i < size; i++) {
      if (neighbors[currentNodeID][i] == arg0.getID()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int degree() {
    return size;
  }

  @Override
  public Node getNeighbor(int arg0) {
    if (neighbors == null) {
      neighbors = new int[indices.length][size];
      for (int i = 0; i < indices.length; i++) {
        for (int j = 0; j < size; j++) {
          neighbors[i][j] = -1;
        }
        nodes[(int)Network.get(i).getID()] = Network.get(i);
      }
      execute();
    }
    int currentNodeId = (int)CommonState.getNode().getID();
    if (neighbors[currentNodeId][arg0] == -1) {
      execute();
    }
    int idx = neighbors[currentNodeId][arg0];
    neighbors[currentNodeId][arg0] = -1;
    return nodes[idx];
  }

  @Override
  public void pack() {
  }
  
  private boolean contains(int id, int nId) {
    for (int i = 0; i < size; i++) {
      if (neighbors[id][i] == nId) {
        return true;
      }
    }
    return false;
  }
  
  private void execute() {
    boolean isOk = false;
    int index = 0;
    while (index < size) {
      for (int i = 0; i < indices.length; i++) {
        indices[i] = i;
      }
      Utils.arraySuffle(CommonState.r, indices);
      for (int nodeId = 0; nodeId < nodes.length; nodeId++) {
        isOk = false;
        for (int nbrIdx = 0; nbrIdx < indices.length; nbrIdx++) {
          if (indices[nbrIdx] != -1 && nodeId != indices[nbrIdx] && !contains(nodeId, indices[nbrIdx])) {
            neighbors[nodeId][index] = indices[nbrIdx];
            indices[nbrIdx] = -1;
            isOk = true;
            break;
          }
        }
        if (!isOk) {
          break;
        }
      }
      if (!isOk) {
        for (int nodeId = 0; nodeId < nodes.length; nodeId++) {
          neighbors[nodeId][index] = -1;
        }
      } else {
        index++;
      }
    }
  }

}
