package gossipLearning.protocols;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.bandits.UCBSZBModel;

import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

public class BanditProtocol extends MultipleLearningProtocol {
  public static final String PAR_WAIT = "numOfWaitingPeriods";
  protected long numOfWaitingPeriods;
  
  protected int numberOfIncomingModels = 0;
  protected ModelHolder localModels;
  
  public BanditProtocol(String prefix) {
    super(prefix);
  }
  
  public BanditProtocol(BanditProtocol a) {
    super(a.prefix, a.delayMean, a.delayVar, a.modelHolderName, a.modelNames);
    if (a.localModels != null) {
      localModels = (ModelHolder)a.localModels.clone();
    }
    numOfWaitingPeriods = a.numOfWaitingPeriods;
    numberOfIncomingModels = a.numberOfIncomingModels;
    clocal = a.clocal;
    isFirstActiveThread = a.isFirstActiveThread;
  }
  
  @Override
  public Object clone() {
    return new BanditProtocol(this);
  }
  
  @Override
  public void init(String prefix) {
    super.init(prefix);
    numOfWaitingPeriods = Configuration.getLong(prefix + "." + PAR_WAIT);
    
    Model model;
    localModels = new BoundedModelHolder(modelNames.length);
    try {
      for (int i = 0; i < modelNames.length; i++) {
        model = (Model)Class.forName(modelNames[i]).newInstance();
        model.init(prefix);
        localModels.add(model);
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }
  
  private boolean isFirstActiveThread = true;
  private long clocal=0;
  @Override
  public void activeThread() {
    // if no incoming model, send the latest one. Else remove the latest one from the queue.
    if (numberOfIncomingModels == 0) {
      if (isFirstActiveThread) {
        numberOfIncomingModels = 1;
      } else {
        clocal++;
      }
    } else {
      for (int i = 0; i < modelHolders.length; i++) {
        modelHolders[i].remove(0);
      }
      clocal = 0;
    }
    if (clocal == numOfWaitingPeriods) {
      System.out.println("ERR:INJECT");
      numberOfIncomingModels = 1;
      clocal = 0;
    }
    // send the models were received in the previous active cycle
    while(numberOfIncomingModels > 0) {
      boolean isSend = true;
      ModelHolder latestModelHolder = new BoundedModelHolder(modelHolders.length);
      for (int i = 0; i < modelHolders.length; i++) {
        // if there is not enough model then not send
        if (numberOfIncomingModels > modelHolders[i].size()) {
          isSend = false;
          System.out.println("ERR:OUTOF");
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
    isFirstActiveThread = false;
  }
  
  @Override
  public void passiveThread(ModelMessage message) {
    // call the message handler of the super class
    super.passiveThread(message);
    // counting the number of incoming models
    numberOfIncomingModels++;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected void updateModels(ModelHolder modelHolder) {
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      Model model = modelHolder.getModel(i);
      // if it is a mergeable model, them merge them
      if (model instanceof Mergeable){
        if (model instanceof UCBSZBModel) {
          model = ((Mergeable<Model>)localModels.getModel(i)).merge(model);
        } else {
          Model lastSeen = lastSeenMergeableModels.getModel(i);
          lastSeenMergeableModels.setModel(i, (Model) model.clone());
          model = ((Mergeable<Model>) model).merge(lastSeen);
        }
      }
      // updating the model with only one randomly selected local training sample
      if (instances != null) {
        int sampleID = CommonState.r.nextInt(instances.size());
        Map<Integer, Double> x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        model.update(x, y);
      }
      // stores the updated model
      if (!(model instanceof UCBSZBModel)) {
        localModels.setModel(i, model);
      }
      modelHolders[i].add(model);
    }
  }
  
  /**
   * Puts the local model, indexed by the specified index, into a new model holder and 
   * returns them.
   */
  @Override
  public ModelHolder getModelHolder(int index) {
    ModelHolder result = new BoundedModelHolder(1);
    result.add(localModels.getModel(index));
    return result;
  }
  
  protected void sendToRandomNeighbor(ModelMessage message) {
    // send to random neighbor
    super.sendToRandomNeighbor(message);
    // perform a circle
    //message.setSource(currentNode);
    //Node randomNode = Network.get(((int)currentNode.getID() + 1)%Network.size());
    //getTransport().send(currentNode, randomNode, message, currentProtocolID);
  }
}
