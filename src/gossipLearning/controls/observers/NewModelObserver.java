package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.adaptive.SelfAdaptiveModelTH;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.reports.GraphObserver;

public class NewModelObserver extends GraphObserver {
  private static final String PAR_PID = "protocol";
  private final int pid;
  
  public NewModelObserver(String prefix) {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PID);
  }

  @Override
  public boolean execute() {
    double numOfNewModels = 0.0;
    double numOfModels = 0.0;
    LearningProtocol protocol;
    for (int i = 0; i < Network.size(); i++) {
      protocol = (LearningProtocol) Network.get(i).getProtocol(pid);
      for (int j = 0; j < protocol.size(); j++) {
        ModelHolder holder = protocol.getModelHolder(j);
        Model model = holder.getModel(holder.size() -1);
        if (model instanceof SelfAdaptiveModelTH) {
          numOfModels ++;
          numOfNewModels += ((SelfAdaptiveModelTH)model).isNewModel() ? 1.0 : 0.0;
        }
      }
    }
    System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + numOfNewModels/numOfModels + "\t#NewModelObserver");
    return false;
  }

}
