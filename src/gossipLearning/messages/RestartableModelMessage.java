package gossipLearning.messages;

import gossipLearning.interfaces.ModelHolder;
import peersim.core.Node;

public class RestartableModelMessage extends ModelMessage {
  private static final long serialVersionUID = 5621412036497937731L;

  private int restartedStepId;
  
  public RestartableModelMessage(Node src, ModelHolder models, int pid, boolean isDeep, int restartedStepId) {
    super(src, models, pid, isDeep);
    this.restartedStepId=restartedStepId;
  }

  public int getRestartedStepId() {
    return restartedStepId;
  }
  
  public void setRestartedStepId(int restartedStepId) {
    this.restartedStepId = restartedStepId;
  }
  
}
