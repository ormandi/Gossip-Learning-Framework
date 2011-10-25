package gossipLearning.controls.initializers;

import gossipLearning.interfaces.LearningProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Protocol;

/**
 * This is an initializer which binds the number of classes computed based on the training and evaluation databases
 * to the number of classes property of the initial models stored by the models. It has to be called after the instance loader
 * to work based on valid data. 
 * 
 * @author Róbert Ormándi
 *
 */
public class SetNumberOfClassesForModels implements Control {
  private static final String PAR_PROT = "protocol";
  private final int pid;
  
  public  SetNumberOfClassesForModels(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }
  
  public boolean execute() {
    // for each learning protocol sets the number of classes for the initial models based on the stored instance holder
    for (int i = 0; i < Network.size(); i++) {
      Protocol learningProtocolP = Network.get(i).getProtocol(pid);
      if (learningProtocolP instanceof LearningProtocol) {
        // get the learning protocol
        LearningProtocol learningProtocol = (LearningProtocol) learningProtocolP;
        
        // for each model holder
        for (int j = 0; j < learningProtocol.size(); j ++) {
          // for each model
          for (int k = 0; learningProtocol.getModelHolder(j) != null && k < learningProtocol.getModelHolder(j).size(); k ++) {
            if (learningProtocol.getModelHolder(j).getModel(k) != null) {
              // sets the number of classes based on the stored instance holder
              learningProtocol.getModelHolder(j).getModel(k).setNumberOfClasses(learningProtocol.getInstanceHolder().getNumberOfClasses());
            }
          }
        }
      } else {
        throw new RuntimeException("The given protocol in initializer setNumberOfClasses.protocol is not a learning protocol!");
      }
    }
    return false;
  }
}
