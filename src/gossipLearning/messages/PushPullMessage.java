package gossipLearning.messages;

import gossipLearning.utils.BQModelHolder;
import peersim.core.Node;

/**
 * ModelMessage for the PushPullLearningProtocol.
 */
public class PushPullMessage extends ModelMessage {

  public final int id;
  public final int updates;
  public final boolean reply;
  
  /** Constructor which creates a deep copy of the models. */
  public PushPullMessage(Node src, BQModelHolder models, int pid, int id, int updates, boolean reply) {
    super(src,models,pid,true);
    this.id = id;
    this.updates = updates;
    this.reply = reply;
  }
  
  @Override
  public PushPullMessage clone() {
    throw new UnsupportedOperationException();
  }
  
}
