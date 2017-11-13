package gossipLearning.messages.tree;

import gossipLearning.messages.Message;
import gossipLearning.models.DummySumLearningModelWithEncryption;
import peersim.core.Node;

public class TreeGradientEncryptionUpMessage implements Message,Cloneable {

  private final Node src;
  private final DummySumLearningModelWithEncryption model;
  private final int pid;
  
  public TreeGradientEncryptionUpMessage(Node src, DummySumLearningModelWithEncryption model, int pid) {
    this.src = src;
    this.model = (DummySumLearningModelWithEncryption)model.clone();
    this.pid = pid;
  }
 
  public Object clone() {
    return new TreeGradientEncryptionUpMessage(src, model, pid);
  }

  public Node getSrc() {
    return src;
  }

  public DummySumLearningModelWithEncryption getModel() {
    return model;
  }

  public int getPid() {
    return pid;
  }

  @Override
  public int getTargetPid() {
    return pid;
  }

}
