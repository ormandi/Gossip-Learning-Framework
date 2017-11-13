package gossipLearning.messages.tree;

import gossipLearning.messages.Message;
import gossipLearning.models.DummySumLearningModel;
import peersim.core.Node;

public class TreeGradientUpMessage implements Message,Cloneable {

  private final Node src;
  private final DummySumLearningModel model;
  private final int pid;
  
  public TreeGradientUpMessage(Node src, DummySumLearningModel model, int pid) {
    this.src = src;
    this.model = (DummySumLearningModel)model.clone();
    this.pid = pid;
  }
 
  public Object clone() {
    return new TreeGradientUpMessage(src, model, pid);
  }

  public Node getSrc() {
    return src;
  }

  public DummySumLearningModel getModel() {
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
