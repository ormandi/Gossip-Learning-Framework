package gossipLearning.utils;

import peersim.core.Node;

public class NodeWithIDCompare implements Comparable<NodeWithIDCompare>{

  private final Node node;
  
  public NodeWithIDCompare(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public int hashCode() {
    final int prime = 997;
    int result = 100;
    result = prime * result + (int)((node == null) ? 0 : node.getID());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NodeWithIDCompare other = (NodeWithIDCompare) obj;
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (node.getID()!=other.node.getID())
      return false;
    return true;
  }

  @Override
  public int compareTo(NodeWithIDCompare o) {
    if(this.node.getID() > o.node.getID()) {
      return 1;
    } else if(this.node.getID()==o.node.getID()) {
      return 0;
    } else {
      return -1; 
    }
  }
  
}
