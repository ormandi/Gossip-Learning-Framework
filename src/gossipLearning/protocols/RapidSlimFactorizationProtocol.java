package gossipLearning.protocols;

import gossipLearning.evaluators.FactorizationResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.messages.ModelMessage;
import peersim.core.CommonState;

public class RapidSlimFactorizationProtocol extends FactorizationProtocolSlim {

  protected int numberOfSentModels;
  protected boolean isFirstCycle;
  
  public RapidSlimFactorizationProtocol(String prefix) {
    super(prefix);
    numberOfSentModels = 0;
    isFirstCycle = true;
  }
  
  protected RapidSlimFactorizationProtocol(RapidSlimFactorizationProtocol a) {
    super(a);
    numberOfSentModels = a.numberOfSentModels;
    isFirstCycle = a.isFirstCycle;
  }
  
  @Override
  public Object clone() {
    return new RapidSlimFactorizationProtocol(this);
  }
  
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        ((FactorizationResultAggregator)resultAggregator).push(currentProtocolID, i, (int)currentNode.getID(), userModels[i], modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
      }
    }
    
    /*if (isFirstCycle) {
    if (CommonState.getTime() < 100) {
      //System.out.println(currentNode.getID());
    } else {
      numberOfIncomingModels = 0;
    }
    }
    isFirstCycle = false;
    */
    // get indices of rated items
    /*if (indices == null) {
      indices = new TreeSet<Integer>();
    } else {
      indices.clear();
    }
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    for (int i = 0; i < instances.size(); i++) {
      for (VectorEntry e : instances.getInstance(i)) {
        indices.add(e.index);
      }
    }*/
    
    // send
    //boolean isWakeUp = false;
    if (numberOfIncomingModels == 0) {
      numberOfWaits ++;
    } else {
      numberOfWaits = 0;
    }
    if (numberOfWaits == numOfWaitingPeriods) {
      numberOfIncomingModels = 1;
      numberOfWaits = 0;
      //isWakeUp = true;
    }
    //if (numberOfIncomingModels > 0) System.out.println("SEND:" + currentNode.getID() + "\t" + numberOfIncomingModels);
    for (int id = numberOfIncomingModels - numberOfSentModels; id > 0; id --) {
      for (int i = 0; i < modelHolders.length; i++) {  
        // store the latest models in a new modelHolder
        Model latestModel = ((Partializable<?>)modelHolders[i].getModel(modelHolders[i].size() - 1)).getModelPart(indices);
        latestModelHolder.add(latestModel);
      }
      if (latestModelHolder.size() == modelHolders.length) {
        // send the latest models to a random neighbor
        sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID, false));
        //System.out.println(currentNode.getID());
      }
      latestModelHolder.clear();
    }
    numberOfIncomingModels = 0;
    numberOfSentModels = 0;
  }
  
  @Override
  protected void updateModels(ModelHolder modelHolder) {
    super.updateModels(modelHolder);
    for (int i = 0; i < modelHolders.length; i++) {
      // store the latest models in a new modelHolder
      //Model latestModel = (MatrixBasedModel)modelHolders[i].getModel(modelHolders[i].size() - id);
      Model latestModel = ((Partializable<?>)modelHolders[i].getModel(modelHolders[i].size() - 1)).getModelPart(indices);
      latestModelHolder.add(latestModel);
    }
    if (latestModelHolder.size() == modelHolders.length) {
      //System.out.println("SEND(U): " + currentNode.getID());
      // send the latest models to a random neighbor
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID, false));
      //System.out.println(currentNode.getID() + " SEND curr " + currentProtocolID);
    }
    latestModelHolder.clear();
    numberOfSentModels ++;
    //System.out.println(currentNode.getID() + "\t" + numberOfIncomingModels);
  }

}
