package gossipLearning.protocols;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.MatrixBasedModel;
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

public class RecSysProtocol2 extends LearningProtocol {

  private SparseVector userModel;
  
  public RecSysProtocol2(String prefix) {
    super(prefix);
  }
  
  protected RecSysProtocol2(RecSysProtocol2 a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new RecSysProtocol2(this);
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
        Model model = (Model)Class.forName(modelNames[i]).newInstance();
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
        ((RecSysResultAggregator)resultAggregator).push(currentProtocolID, i, (int)currentNode.getID(), userModel, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
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
        Model latestModel = ((Partializable<?>)modelHolders[i].getModel(modelHolders[i].size() - 1)).getModelPart(indices);
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
      MatrixBasedModel recvModel = (MatrixBasedModel)modelHolder.getModel(i);
      MatrixBasedModel currModel = (MatrixBasedModel)modelHolders[i].getModel(0);
      // if it is a mergeable model, them merge them
      if (currModel instanceof Mergeable){
        currModel = (MatrixBasedModel)((Mergeable) currModel).merge(recvModel);
      }
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        userModel = currModel.update((int)currentNode.getID(), userModel, x);
      }
      // stores the updated model
      modelHolders[i].add(currModel);
    }
  }
  
  @Override
  public void setNumberOfClasses(int numberOfClasses) {
  }

}
