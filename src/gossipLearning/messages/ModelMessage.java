package gossipLearning.messages;

import gossipLearning.interfaces.Model;
import peersim.core.Node;

@Message
public class ModelMessage implements ModelHolder {
  private final Node src;
  
  // TODO: Implements the methods from interface ModelHolder
  public ModelMessage(Node src) {
    this.src = src;
  }
  
  public Node getSource() {
    return src;
  }
}
