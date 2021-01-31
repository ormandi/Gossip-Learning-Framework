package gossipLearning.protocols;

import peersim.core.*;
import peersim.config.*;
import peersim.transport.*;
import peersim.util.RandPermutation;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.messages.ModelMessage;

public class TokenLearningProtocol extends LearningProtocol {

  //---------------------------------------------------------------------
  //Parameters
  //---------------------------------------------------------------------

  /** 
   * The token strategy. ("proactive", "traditional", "generalized", "randomized")
   * @config
   */
  private static final String PAR_STRAT = "strategy";

  /** 
   * Parameter A of the chosen token strategy.
   * @config
   */
  private static final String PAR_A = "A";

  /** 
   * Parameter B of the chosen token strategy.
   * @config
   */
  private static final String PAR_B = "B";
  
  /** 
   * The neighbour sampling strategy. 0 - don't use permutation (independent sampling); 1 - use permutation (it's re-generated after use)
   * @config
   */
  private static final String PAR_PERM = "permutation";
  
  private static final String PAR_WT = "warmupTime";
  
  //---------------------------------------------------------------------
  //Fields
  //---------------------------------------------------------------------

  protected RandPermutation neighborPerm = new RandPermutation(CommonState.r);
  private int token;
  protected final String strategy;
  protected final int A;
  protected final int B;
  protected final int permMode;
  protected final long warmupTime;
  
  //---------------------------------------------------------------------
  //Initialization
  //---------------------------------------------------------------------

  public TokenLearningProtocol(String prefix) {
    super(prefix);
    strategy = Configuration.getString(prefix+"."+PAR_STRAT);
    A = Configuration.getInt(prefix+"."+PAR_A);
    B = Configuration.getInt(prefix+"."+PAR_B);
    permMode = Configuration.getInt(prefix+"."+PAR_PERM);
    warmupTime = Configuration.getLong(prefix+"."+PAR_WT,0);
  }

  /**
   * Copy constructor.
   */
  public TokenLearningProtocol(TokenLearningProtocol a) { // a new neighborPerm instance is used
    super(a);
    token = a.token;
    strategy = a.strategy;
    A = a.A;
    B = a.B;
    permMode = a.permMode;
    warmupTime = a.warmupTime;
  }
  
  @Override
  public TokenLearningProtocol clone() {
    return new TokenLearningProtocol(this);
  }

  //---------------------------------------------------------------------
  //Methods
  //---------------------------------------------------------------------
  
  @Override
  public void activeThread() {
    if (!nodeIsOnline(currentNode,currentProtocolID))
      return;
    evaluate();
    token++;
    if (CommonState.r.nextDouble()<proChance(token))
      sendMsg();
  }
  
  @Override
  protected void updateModels(ModelHolder modelHolder) {
    int x = (int)Math.floor(CommonState.r.nextDouble()+reactions(token,updateState(modelHolder)));
    for (int i=0; i<x; i++)
      sendMsg();
  }
  
  protected boolean updateState(ModelHolder modelHolder) {
    if (CommonState.getTime()<warmupTime)
      return true;
    for (int i=0; i<modelHolder.size(); i++) {
      Model recvModel = modelHolder.getModel(i);
      if (recvModel instanceof Mergeable) {
        ((Mergeable)models[i]).merge(recvModel);
      } else {
        models[i] = recvModel;
      }
      ((LearningModel)models[i]).update(instances,epoch,batch);
    }
    return true;
  }
  
  private void sendMsg() {
    modelHolder.clear();
    for (int i=0; i<models.length; i++) {
      Model model = models[i];
      if (model instanceof Partializable)
        model = ((Partializable)model).getModelPart();
      modelHolder.add(model);
    }
    // send the latest models to a random online neighbor
    --token;
    assert token>=0;
    Linkable linkable = getOverlay();
    if (permMode==0)
      neighborPerm.reset(linkable.degree());
    for (int i=0; i<2; i++) {
      while (neighborPerm.hasNext()) {
        Node target = linkable.getNeighbor(neighborPerm.next());
        if (nodeIsOnline(target,currentProtocolID)) {
          getTransport().send(currentNode,target,new ModelMessage(currentNode,target,modelHolder,currentProtocolID,true),currentProtocolID);
          return;
        }
      }
      neighborPerm.reset(linkable.degree());
    }
  }
  
  // Token strategy
  
  protected double proChance(int t) {
    switch (strategy) {
      case "proactive": return 1;
      case "traditional": return t>B?1:0;
      case "generalized": return t>B?1:0;
      case "randomized": return (double)(t-A)/(B+1-A);
      default: throw new RuntimeException("Invalid strategy!");
    }
  }
  
  protected double reactions(int t, boolean changed) {
    switch (strategy) {
      case "proactive": return 0;
      case "traditional": return t>0?1:0;
      case "generalized": return (A-1+t)/(changed?A:2*A);
      case "randomized": return changed?(double)t/A:0;
      default: throw new RuntimeException("Invalid strategy!");
    }
  }
  
}
