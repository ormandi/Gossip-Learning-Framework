package gossipLearning.messages;

import gossipLearning.utils.NodeDescriptor;
import peersim.core.Node;

public class TManMessage implements Message {
  public final Node src;
  public final boolean isAnswer;
  public final NodeDescriptor[] cache;

  public TManMessage(NodeDescriptor srcDesc, NodeDescriptor[] cache, boolean isAnswer) {
    this.src = (Node)srcDesc.getNode().clone();
    this.isAnswer = isAnswer;
    this.cache = new NodeDescriptor[cache.length];
    for (int i = 0; i < cache.length -1 && cache[i] != null; i++) {
      this.cache[i + 1] = (NodeDescriptor)cache[i].clone();
    }
    this.cache[0] = (NodeDescriptor)srcDesc.clone();
  }

  @Override
  public int getTargetPid() {
    return 0;
  }

}
