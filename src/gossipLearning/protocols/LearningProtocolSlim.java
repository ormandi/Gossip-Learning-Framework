package gossipLearning.protocols;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;
import java.util.TreeSet;

import peersim.core.CommonState;

public class LearningProtocolSlim extends LearningProtocol {
  
  public LearningProtocolSlim(String prefix) {
    super(prefix);
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
      resultAggregator = new RecSysResultAggregator(modelNames, evalNames);
      // holder for storing the last seen mergeable models for correct merge
      lastSeenMergeableModels = null;
      modelHolders = new ModelHolder[modelNames.length];
      latestModelHolder = new BQModelHolder(modelNames.length);
      for (int i = 0; i < modelNames.length; i++){
        try {
          modelHolders[i] = (ModelHolder)Class.forName(modelHolderName).getConstructor(int.class).newInstance(1);
        } catch (NoSuchMethodException e) {
          modelHolders[i] = (ModelHolder)Class.forName(modelHolderName).newInstance();
        }
        LearningModel model = (LearningModel)Class.forName(modelNames[i]).newInstance();
        model.init(prefix);
        modelHolders[i].add(model);
      }
      numberOfIncomingModels = 1;
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }
  
  protected ModelHolder latestModelHolder;
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        ((ResultAggregator)resultAggregator).push(currentProtocolID, i, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
      }
    }
    
    // get indices of rated items
    Set<Integer> indices = new TreeSet<Integer>();
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    for (int i = 0; i < instances.size(); i++) {
      for (VectorEntry e : instances.getInstance(i)) {
        indices.add(e.index);
      }
    }
    
    // send
    for (int id = 1; id > 0; id --) {
      latestModelHolder.clear();
      for (int i = 0; i < modelHolders.length; i++) {  
        // store the latest models in a new modelHolder
        Model latestModel = ((Partializable<?>)modelHolders[i].getModel(0)).getModelPart(indices);
        latestModelHolder.add(latestModel);
      }
      if (latestModelHolder.size() == modelHolders.length) {
        // send the latest models to a random neighbor
        sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID));
      }
    }
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
      // if it is a mergeable model, them merge them
      ((Mergeable) currModel).merge(recvModel);
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        double label = instances.getLabel(sampleID);
        currModel.update(x, label);
      }
      // stores the updated model (not necessary since it has only 1 model)
      //modelHolders[i].add(currModel);
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
