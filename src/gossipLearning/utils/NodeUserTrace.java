package gossipLearning.utils;

import peersim.core.Node;

public class NodeUserTrace {
  final private UserTrace ut;
  final private Node node;
  
  public NodeUserTrace(UserTrace ut, Node node) {
    this.ut = ut;
    this.node = node;
  }
  
  public Node getNode() {
    return node;
  }
  
  public Session nextSession() {
    return ut.nextSession();
  }
  
  public Boolean hasMoreSession() {
    return ut.hasMoreSession();
  }
}
