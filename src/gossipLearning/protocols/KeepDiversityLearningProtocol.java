package gossipLearning.protocols;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;

/**
 * This class is such a kind of MultipleOneLearningProtovol that sends all of models, 
 * were received in the last active cycle, to randomly selected neighbors, in order to 
 * keep the diversity of models. <br/>
 * If there is no received model at a node in the last cycle, then sends the last 
 * received model.
 * @author István Hegedűs
 *
 */
public class KeepDiversityLearningProtocol extends MultipleOneLearningProtocol {
  
  protected int numberOfIncomingModels;
  public KeepDiversityLearningProtocol (String prefix) {
    super(prefix);
    numberOfIncomingModels = 0;
  }
  
  protected KeepDiversityLearningProtocol (KeepDiversityLearningProtocol a) {
    super(a);
    numberOfIncomingModels = 0;
  }
  
  public Object clone() {
    return new KeepDiversityLearningProtocol(this);
  }
  
  public void activeThread() {
    // if no incoming model, send the latest one. Else remove the latest one from the queue.
    if (numberOfIncomingModels == 0) {
      numberOfIncomingModels = 1;
    } else {
      for (int i = 0; i < modelHolders.length; i++) {
        modelHolders[i].remove(0);
      }
    }
    // send the models were received in the previous active cycle
    while(numberOfIncomingModels > 0) {
      boolean isSend = true;
      ModelHolder latestModelHolder = new BoundedModelHolder(modelHolders.length);
      for (int i = 0; i < modelHolders.length; i++) {
        // if there is not enough model then not send
        if (numberOfIncomingModels > modelHolders[i].size()) {
          isSend = false;
          continue;
        }
        // store the latest models in a new modelHolder, and remove all but the latest
        Model latestModel;
        if (modelHolders[i].size() > 1) {
          latestModel = modelHolders[i].remove(modelHolders[i].size() - numberOfIncomingModels);
        } else {
          latestModel = modelHolders[i].getModel(modelHolders[i].size() - numberOfIncomingModels);
        }
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
