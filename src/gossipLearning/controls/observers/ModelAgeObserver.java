package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.adaptive.SelfAdaptiveModel;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class ModelAgeObserver implements Control {
  private static final String PAR_PID = "protocol";
  private final int pid;
  
  public ModelAgeObserver(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
  }

  @Override
  public boolean execute() {
    LearningProtocol protocol;
    System.out.print("##");
    for (int i = 0; i < Network.size(); i++) {
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
