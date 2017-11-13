package gossipLearning.utils;

import peersim.core.Node;

public class TracePointer {
  final UserTraceLow ut;
  final Node node;
  int pointer = 0;
  
  public TracePointer(UserTraceLow ut, Node node) {
    this.ut = ut;
    this.node = node;
  }
  
  public Node getNode() {
    return node;
  }
  
  public Session nextSession() {
    return ut.sessions[pointer++];
  }
  
  public Boolean hasMoreSession() {
    return pointer < ut.sessions.length;
  }
}
