package gossipLearning.messages;

import gossipLearning.interfaces.Model;
import peersim.core.Node;

@Message
public class ModelMessage<I> {
  private final Node src;
  private final Model<I> m;
  
  @SuppressWarnings("unchecked")
  public ModelMessage(Node src, Model<I> m) {
    this.src = src;
    //this.m = (M) ((gossipLearning.utils.Cloneable<M>) m).clone();
    this.m = (Model<I>) m.clone();
  }
  
  public Node getSource() {
    return src;
  }
  
  public Model<I> getModel() {
    return m;
  }
}
