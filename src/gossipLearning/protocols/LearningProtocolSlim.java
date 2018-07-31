package gossipLearning.protocols;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.InstanceHolder;
import peersim.core.CommonState;

public class LearningProtocolSlim extends LearningProtocol {
  
  public LearningProtocolSlim(String prefix) {
    super(prefix, 1);
  }
  
  protected LearningProtocolSlim(LearningProtocolSlim a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new LearningProtocolSlim(this);
  }
  
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        ((ResultAggregator)resultAggregator).push(currentProtocolID, i, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(extractorProtocolID)).getModel());
      }
    }
    
    // send
    for (int i = 0; i < modelHolders.length; i++) {  
      // store the latest models in a new modelHolder
      Model latestModel = ((Partializable)modelHolders[i].getModel(0)).getModelPart();
      latestModelHolder.add(latestModel);
    }
    // send the latest models to a random neighbor
    sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID, false));
    latestModelHolder.clear();
    numberOfIncomingModels = 0;
  }
  
  protected void updateModels(ModelHolder modelHolder){
    // get instances from the extraction protocol
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(extractorProtocolID)).getInstances();
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      LearningModel recvModel = (LearningModel)modelHolder.getModel(i);
      LearningModel currModel = (LearningModel)modelHolders[i].getModel(0);
      // it works only with mergeable models, and merge them
      ((Mergeable) currModel).merge(recvModel);
      // updating the model with the local training samples
      currModel.update(instances, epoch, batch);
      // stores the updated model (not necessary since it has only 1 model)
      modelHolders[i].add(currModel);
    }
  }

}
