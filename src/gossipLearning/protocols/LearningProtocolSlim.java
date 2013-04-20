package gossipLearning.protocols;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;
import java.util.TreeSet;

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
  
  public void init(String prefix) {
    try {
      super.init(prefix);
      resultAggregator = new ResultAggregator(modelNames, evalNames);
      lastSeenMergeableModels = null;
      numberOfIncomingModels = 1;
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }
  
  protected Set<Integer> indices;
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        ((ResultAggregator)resultAggregator).push(currentProtocolID, i, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
      }
    }
    
    // get indices of rated items
    if (indices == null) {
      indices = new TreeSet<Integer>();
    } else {
      indices.clear();
    }
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    for (int i = 0; i < instances.size(); i++) {
      for (VectorEntry e : instances.getInstance(i)) {
        indices.add(e.index);
      }
    }
    
    // send
    for (int i = 0; i < modelHolders.length; i++) {  
      // store the latest models in a new modelHolder
      Model latestModel = ((Partializable<?>)modelHolders[i].getModel(0)).getModelPart(indices);
      latestModelHolder.add(latestModel);
    }
    if (latestModelHolder.size() == modelHolders.length) {
      // send the latest models to a random neighbor
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID));
    }
    latestModelHolder.clear();
    numberOfIncomingModels = 0;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void updateModels(ModelHolder modelHolder){
    // get instances from the extraction protocol
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    if (instances.size() > 1) {
      throw new RuntimeException("The number of instances should be one at avery node instead of " + instances.size());
    }
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      LearningModel recvModel = (LearningModel)modelHolder.getModel(i);
      LearningModel currModel = (LearningModel)modelHolders[i].getModel(0);
      // it works only with mergeable models, and merge them
      ((Mergeable) currModel).merge(recvModel);
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        double label = instances.getLabel(sampleID);
        currModel.update(x, label);
      }
      // stores the updated model (not necessary since it has only 1 model)
      modelHolders[i].add(currModel);
    }
  }
  
  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    for (int i = 0; i < modelHolders.length; i++) {
      for (int j = 0; j < modelHolders[i].size(); j++) {
        ((LearningModel)modelHolders[i].getModel(j)).setNumberOfClasses(numberOfClasses);
      }
    }
  }
}
