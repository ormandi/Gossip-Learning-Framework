package gossipLearning.messages;

import gossipLearning.utils.NodeDescriptor;
import peersim.core.Node;

/**
 * This class specifies a kind of message for the TMan protocol.
 * @author István Hegedűs
 */
public class TManMessage implements Message {
  /**
   * Node to be described.
   */
  public final Node src;
  /**
   * This message is an answer or not.
   */
  public final boolean isAnswer;
  /**
   * The neighborhood descriptors of the described node.
   */
  public final NodeDescriptor[] cache;
  private final int pid;

  /**
   * Constructs a message based on the specified parameters, cache will be cloned.
   * @param srcDesc descriptor of the described node.
   * @param cache neighborhood descriptors
   * @param isAnswer the message is an answer or not
   */
  public TManMessage(NodeDescriptor srcDesc, NodeDescriptor[] cache, boolean isAnswer) {
    this(srcDesc, cache, isAnswer, 0);
  }
  
  /**
   * Constructs a message based on the specified parameters, cache will be cloned.
   * @param srcDesc descriptor of the described node.
   * @param cache neighborhood descriptors
   * @param isAnswer the message is an answer or not
   * @param pid pid of the target protocol
   */
  public TManMessage(NodeDescriptor srcDesc, NodeDescriptor[] cache, boolean isAnswer, int pid) {
    this.src = srcDesc.getNode();
    this.isAnswer = isAnswer;
    this.cache = new NodeDescriptor[cache.length];
    for (int i = 0; i < cache.length -1 && cache[i] != null; i++) {
      this.cache[i + 1] = cache[i].clone();
    }
    this.cache[0] = srcDesc.clone();
    this.pid = pid;
  }

  @Override
  public int getTargetPid() {
    return pid;
  }

}
