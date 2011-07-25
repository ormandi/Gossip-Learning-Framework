package gossipLearning.messages;

import gossipLearning.interfaces.Model;
import peersim.core.Node;

@Message
public class ModelMessage<M extends Model<?>> {
  private final Node src;
  private final M m;
  
  @SuppressWarnings("unchecked")
  public ModelMessage(Node src, M m) {
    this.src = src;
    this.m = (M) m.clone();
  }
  
  public Node getSource() {
    return src;
  }
  
  public M getModel() {
    return m;
  }
}
