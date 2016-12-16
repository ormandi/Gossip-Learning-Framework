package gossipLearning.messages;

import gossipLearning.interfaces.models.Model;
import peersim.core.Node;

public class RestartableSoloModelMessage implements Model, Message {
  private static final long serialVersionUID = 5621412036497937731L;

  private int restartedStepId;
  /** @hidden */
  private final Node src;
  /** @hidden */
  private final Model model;
  private final int pid;
  
  public RestartableSoloModelMessage(Node src, Model model, int pid, int restartedStepId) {
    this.src = src;
    this.model = (Model)model.clone();
    this.pid = pid;
    this.restartedStepId=restartedStepId;
  }

  public Object clone() {
    return new RestartableSoloModelMessage(this.src, this.model, this.pid, this.restartedStepId);
  }
  
  public int getRestartedStepId() {
    return restartedStepId;
  }
  
  public void setRestartedStepId(int restartedStepId) {
    this.restartedStepId = restartedStepId;
  }

  
  public Node getSource() {
    return src;
  }
  
  @Override
  public int getTargetPid() {
    return pid;
  }

  public Model getModel() {
    return model;
  }

  @Override
  public double getAge() {
    return model.getAge();
  }
  
}
