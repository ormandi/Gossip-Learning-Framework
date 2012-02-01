package gossipLearning.protocols;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;

/**
 * This class is such a kind of MultipleOneLearningProtovol that sends all of models, 
 * were received in the last active cycle, to randomly selected neighbors, in order to 
 * keep the diversity of models.
 * @author István Hegedűs
 *
 */
public class KeepDiversityLearningProtocol extends MultipleOneLearningProtocol {
  
  protected int numberOfIncomingModels;
  public KeepDiversityLearningProtocol (String prefix) {
    super(prefix);
    numberOfIncomingModels = 1;
  }
  
  protected KeepDiversityLearningProtocol (KeepDiversityLearningProtocol a) {
    super(a);
    numberOfIncomingModels = 1;
  }
  
  public Object clone() {
    return new KeepDiversityLearningProtocol(this);
  }
  
  public void activeThread() {
    // send the models were received in the previous active cycle
    while(numberOfIncomingModels > 0) {
      boolean isSend = true;
      ModelHolder latestModelHolder = new BoundedModelHolder(modelHolders.length);
      for (int i = 0; i < modelHolders.length; i++) {
        // if there is not enough model then not send
        if (numberOfIncomingModels > modelHolders[i].size()) {
          isSend = false;
          break;
        }
        // store the latest models in a new modelHolder
        Model latestModel = modelHolders[i].getModel(modelHolders[i].size() - numberOfIncomingModels);
        latestModelHolder.add(latestModel);
      }
      
      if (isSend) {
        // send the latest models to a random neighbor
        sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
      }
      numberOfIncomingModels--;
    }
  }
  
  public void passiveThread(ModelMessage message) {
    // call the message handler of the super class
    super.passiveThread(message);
    // counting the number of incoming models
    numberOfIncomingModels++;
  }
  
}
