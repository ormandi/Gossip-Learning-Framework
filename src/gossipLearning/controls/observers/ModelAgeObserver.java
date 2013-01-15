package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.adaptive.SelfAdaptiveModel;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;

public class ModelAgeObserver implements Control {
  private static final String PAR_PID = "protocol";
  private final int pid;
  
  private static final String PAR_ONLINE = "onlineOnly";
  protected final boolean onlineOnly;
  
  public ModelAgeObserver(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
    onlineOnly = Configuration.getBoolean(prefix + "." + PAR_ONLINE);
  }

  @Override
  public boolean execute() {
    LearningProtocol protocol;
    System.out.print("##");
    for (int i = 0; i < Network.size(); i++) {
      int state = ((Node)Network.get(i)).getFailState();
      if (onlineOnly && (state == Fallible.DEAD || state == Fallible.DOWN)) {
        continue;
      }
      protocol = (LearningProtocol) Network.get(i).getProtocol(pid);
      for (int j = 0; j < protocol.size(); j++) {
        ModelHolder holder = protocol.getModelHolder(j);
        Model model = holder.getModel(holder.size() -1);
        if (model instanceof SelfAdaptiveModel) {
          System.out.print(" " + ((SelfAdaptiveModel)model).getAge());
        }
      }
    }
    System.out.println();
    return false;
  }

}
