package gossipLearning.protocols;

import gossipLearning.evaluators.FactorizationResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Linkable;
import peersim.core.Node;

public class FactorizationProtocolSlim extends LearningProtocol {
  protected static final String PAR_ARRGNAME = "aggrName";
  
  /**
   * One user model for every model
   */
  protected SparseVector[] userModels;
  //protected String aggrClassName;
  
  public FactorizationProtocolSlim(String prefix) {
    // sets the holder capacity to 1
    super(prefix, 1);
    String aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
    try {
      resultAggregator = (FactorizationResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    //lastSeenMergeableModels = null;
    userModels = new SparseVector[modelNames.length];
  }
  
  protected FactorizationProtocolSlim(FactorizationProtocolSlim a) {
    super(a);
    resultAggregator = (FactorizationResultAggregator)a.resultAggregator.clone();
    userModels = new SparseVector[a.userModels.length];
    for (int i = 0; i < userModels.length; i++) {
      if (a.userModels[i] != null) {
        userModels[i] = (SparseVector)a.userModels[i].clone();
      }
    }
    //System.out.println(numberOfIncomingModels);
  }
  
  @Override
  public Object clone() {
    return new FactorizationProtocolSlim(this);
  }
  
  protected Set<Integer> indices;
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        ((FactorizationResultAggregator)resultAggregator).push(currentProtocolID, i, (int)currentNode.getID(), userModels[i], modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(extractorProtocolID)).getModel());
      }
    }
    
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
      //numRestarts ++;
      //isWakeUp = true;
    }
    //if (numberOfIncomingModels > 0) System.out.println("SEND:" + currentNode.getID() + "\t" + numberOfIncomingModels);
    for (int id = numberOfIncomingModels; id > 0; id --) {
      for (int i = 0; i < modelHolders.length; i++) {  
        // store the latest models in a new modelHolder
        Model latestModel = ((Partializable)modelHolders[i].getModel(modelHolders[i].size() - 1)).getModelPart();
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
  }
  
  protected void updateModels(ModelHolder modelHolder){
    //numIncomingModels ++;
    //System.out.println("RECV:" + currentNode.getID());
    // get instances from the extraction protocol
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(extractorProtocolID)).getInstances();
    if (instances.size() > 1) {
      throw new RuntimeException("The number of instances should be one at avery node instead of " + instances.size());
    }
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      MatrixBasedModel recvModel = (MatrixBasedModel)modelHolder.getModel(i);
      MatrixBasedModel currModel = (MatrixBasedModel)modelHolders[i].getModel(0);
      // it works only with mergeable models, and merge them
      ((Mergeable) currModel).merge(recvModel);
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        userModels[i] = currModel.update((int)currentNode.getID(), userModels[i], x);
      }
      // stores the updated model
      //modelHolders[i].add(currModel);
    }
  }
  
  private Node[] onlines = null;
  protected void sendToRandomNeighbor(ModelMessage message) {
    //numSentModels ++;
    //System.out.println("SEND");
    Linkable overlay = getOverlay();
    if (onlines == null) {
      onlines = new Node[overlay.degree()];
    }
    int numOnlines = 0;
    Node randomNode = null;
    for (int i = 0; i < overlay.degree(); i++) {
      Node n = overlay.getNeighbor(i);
      if (n.getFailState() == Fallible.OK) {
        onlines[numOnlines] = n;
        numOnlines ++;
      }
    }
    if (numOnlines != 0) {
      randomNode = onlines[CommonState.r.nextInt(numOnlines)];
    } else {
      randomNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
    }
    getTransport().send(currentNode, randomNode, message, currentProtocolID);
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
  }

}
