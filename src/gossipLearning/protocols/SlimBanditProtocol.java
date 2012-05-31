package gossipLearning.protocols;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.bandits.P2GreedyModel;
import gossipLearning.models.bandits.P2GreedySlim;

public class SlimBanditProtocol extends SimpleBanditProtocol2SentModels {
  
  public SlimBanditProtocol(String prefix) {
    super(prefix);
  }
  
  protected SlimBanditProtocol(SimpleBanditProtocol2SentModels a) {
    super(a);
  }
  
  public Object clone() {
    return new SlimBanditProtocol(this);
  }
  
  @Override
  public void activeThread() {
    // check whether the node has at least one model
    if (getModelHolder(0) != null && getModelHolder(0).size() > 0){
      
      // check the model stored in the holder
      final Model latestModelG = getModelHolder(0).getModel(getModelHolder(0).size() - 1);
      if (! (latestModelG instanceof P2GreedySlim)) {
        throw new RuntimeException("This protocol supports only the P2GreedySlim models!!!");
      }
      
      // get the latest model from the holder
      final P2GreedySlim latestModel = (P2GreedySlim) latestModelG;
      
      // get the stored model of the current peer
      final P2GreedySlim storedModel = latestModel.getMyModel();
      
      
      // get the model with higher expected reward
      Model M = latestModel; 
      if (storedModel != null) {
        M = (latestModel.predict(latestModel.getI()) >= storedModel.predict(storedModel.getI())) ? latestModel : storedModel;
      } else {
        System.err.println("My model is null!!!");
      }
      
      ModelHolder sendingModelHolder = new BoundedModelHolder(1);
      sendingModelHolder.add(M);
      
      // send the latest model to a random neighbor
      sendToNeighbor(new ModelMessage(currentNode, sendingModelHolder), 0);
      sendToNeighbor(new ModelMessage(currentNode, sendingModelHolder), 1);
    }
  }
}
