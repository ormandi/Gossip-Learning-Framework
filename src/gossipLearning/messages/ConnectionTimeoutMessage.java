package gossipLearning.messages;

import peersim.core.Node;

public class ConnectionTimeoutMessage implements Message {
 
  protected final Node src;

  public ConnectionTimeoutMessage(Node src) {
    this.src=src;
  }
  
  @Override
  public int getTargetPid() {
    return 0;
  }

  public Node getSource() {
    return src;
  }
  
}
