package gossipLearning.messages.multiwalker;

import gossipLearning.interfaces.models.MultiLearningModel;
import gossipLearning.messages.Message;
import peersim.core.Node;

public class RestartableModelMessage implements Message {
  /** @hidden */
  private final Node src;
  /** @hidden */
  private final MultiLearningModel model;
  private final int pid;
  
  public RestartableModelMessage(Node src, MultiLearningModel model, int pid) {
    this.src = src;
    this.model = (MultiLearningModel)model.clone();
    this.pid = pid;
  }

  public Object clone() {
    return new RestartableModelMessage(this.src, this.model, this.pid);
  }

  
  public Node getSource() {
    return src;
  }
  
  @Override
  public int getTargetPid() {
    return pid;
  }

  public MultiLearningModel getModel() {
    return model;
  }
  
}
