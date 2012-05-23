package gossipLearning.protocols;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.bandits.BanditModel;

public class SimpleBanditProtocol2SentModels extends SimpleLearningProtocol {
  private final String prefix;
  public SimpleBanditProtocol2SentModels(String prefix) {
    super(prefix);
    this.prefix = prefix;
  }
  
  public Object clone() {
    return new SimpleBanditProtocol2SentModels(prefix);
  }
  
  @Override
  public void activeThread() {
    // check whether the node has at least one model
    if (getModelHolder(0) != null && getModelHolder(0).size() > 0){
      
      // store the latest model in a new modelHolder
      Model latestModel = getModelHolder(0).getModel(getModelHolder(0).size() - 1);
      ModelHolder latestModelHolder = new BoundedModelHolder(1);
      latestModelHolder.add(latestModel);
      
      // send the latest model to a random neighbor
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
    }
  }
  
  @Override
  public void passiveThread(ModelMessage message) {
    for (int incommingModelID = 0; message != null && incommingModelID < message.size(); incommingModelID ++) {
      // process each model that can be found in the message (they are clones, so not necessary to copy them again)
      Model model = message.getModel(incommingModelID);
      
      // this protocol can work with Bandit models
      if (! (model instanceof BanditModel)) {
        throw new RuntimeException("BanditProtocol can work only with BanditModels!");
      }
      
      // update 
      model.update(null, 0.0);
      
      // model is updated based on the Global arm model => store it
      getModelHolder(0).add(model);
    }
  }

}
