package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partitioned;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.BQModelHolder;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.util.RandPermutation;

public class PartitionedTokenLearningProtocol extends TokenLearningProtocol {

  //---------------------------------------------------------------------
  //Parameters
  //---------------------------------------------------------------------
  
  /** 
   * Subsampling factor.
   * @config
   */
  private static final String PAR_NP = "numParts";
  
  //---------------------------------------------------------------------
  //Fields
  //---------------------------------------------------------------------

  private RandPermutation partPerm = new RandPermutation(CommonState.r);
  private final int numParts;
  private int[] token;
  
  //---------------------------------------------------------------------
  //Initialization
  //---------------------------------------------------------------------

  public PartitionedTokenLearningProtocol(String prefix) {
    super(prefix);
    numParts = Configuration.getInt(prefix+"."+PAR_NP);
    token = new int[numParts];
  }

  /**
   * Copy constructor.
   */
  public PartitionedTokenLearningProtocol(PartitionedTokenLearningProtocol a) { // a new partPerm instance is used
    super(a);
    numParts = a.numParts;
    token = a.token.clone();
  }
  
  @Override
  public PartitionedTokenLearningProtocol clone() {
    return new PartitionedTokenLearningProtocol(this);
  }

  //---------------------------------------------------------------------
  //Methods
  //---------------------------------------------------------------------
  
  @Override
  public void activeThread() {
    if (!nodeIsOnline(currentNode))
      return;
    evaluate();
    if (!partPerm.hasNext())
      partPerm.reset(numParts);
    int pi = partPerm.next();
    token[pi]++;
    if (CommonState.r.nextDouble()<proChance(token[pi]))
      sendMsg(pi);
  }
  
  @Override
  protected void updateModels(ModelHolder modelHolder){
    int pi = ((PartitionedTokenMessage)modelHolder).partIndex;
    int x = (int)Math.floor(CommonState.r.nextDouble()+reactions(token[pi],updateState(modelHolder)));
    for (int i=0; i<x; i++)
      sendMsg(pi);
  }
  
  private void sendMsg(int pi) {
    modelHolder.clear();
    for (int i=0; i<models.length; i++) {
      Model model = models[i];
      model = ((Partitioned)model).getModelPart(pi);
      modelHolder.add(model);
    }
    // send the latest models to a random online neighbor
    --token[pi];
    assert token[pi]>=0;
    Linkable linkable = getOverlay();
    if (permMode==0)
      neighborPerm.reset(linkable.degree());
    for (int i=0; i<2; i++) {
      while (neighborPerm.hasNext()) {
        Node target = linkable.getNeighbor(neighborPerm.next());
        if (nodeIsOnline(target)) {
          getTransport().send(currentNode,target,new PartitionedTokenMessage(currentNode,target,modelHolder,currentProtocolID,true,pi),currentProtocolID);
          return;
        }
      }
      neighborPerm.reset(linkable.degree());
    }
  }
  
}

class PartitionedTokenMessage extends ModelMessage {

  public final int partIndex;

  public PartitionedTokenMessage(Node src, Node dst, BQModelHolder models, int pid, boolean deep, int pi) {
    super(src,dst,models,pid,deep);
    partIndex = pi;
  }

}
