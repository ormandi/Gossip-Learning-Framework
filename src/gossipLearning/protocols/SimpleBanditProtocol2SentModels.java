package gossipLearning.protocols;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.bandits.BanditModel;
import gossipLearning.models.bandits.GlobalArmModel;

import java.util.Arrays;

public class SimpleBanditProtocol2SentModels extends SimpleLearningProtocol {
  private final String prefix;
  
  /** @hidden */
  protected Mergeable<? extends BanditModel> lastSeenMergeableModel;
  
  protected double[] armHits;
  protected double sumHits;
  
  
  public SimpleBanditProtocol2SentModels(String prefix) {
    super(prefix);
    this.prefix = prefix;
    armHits = new double[GlobalArmModel.numberOfArms()];
    Arrays.fill(armHits, 0.0);
    sumHits = 0.0;
    lastSeenMergeableModel = null;
  }
  
  @SuppressWarnings("unchecked")
  protected SimpleBanditProtocol2SentModels(SimpleBanditProtocol2SentModels a) {
    super(a.prefix);
    prefix = a.prefix;
    armHits = Arrays.copyOf(a.armHits, a.armHits.length);
    sumHits = a.sumHits;
    if (a.lastSeenMergeableModel != null) {
      lastSeenMergeableModel = (Mergeable<? extends BanditModel>)((BanditModel)a.lastSeenMergeableModel).clone();
    }
  }
  
  public Object clone() {
    return new SimpleBanditProtocol2SentModels(this);
  }
  
  @Override
  public void activeThread() {
    // check whether the node has at least one model
    if (getModelHolder(0) != null && getModelHolder(0).size() > 0){
      
      // store the latest model in a new modelHolder
      Model latestModel = getModelHolder(0).getModel(getModelHolder(0).size() - 1);
      ModelHolder latestModelHolder = new BoundedModelHolder(1);
      latestModelHolder.add(latestModel);
      
      // send the latest model to a random neighbor
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
    }
  }
  
  @SuppressWarnings({ "unchecked" })
  @Override
  public void passiveThread(ModelMessage message) {
    for (int incommingModelID = 0; message != null && incommingModelID < message.size(); incommingModelID ++) {
      // process each model that can be found in the message (they are clones, so not necessary to copy them again)
      Model m = message.getModel(incommingModelID);
      
      // this protocol can work with Bandit models
      if (! (m instanceof BanditModel)) {
        throw new RuntimeException("BanditProtocol can work only with BanditModels!");
      }
      
      BanditModel model = (BanditModel)m;
      // store the model as last seen
      if (model instanceof Mergeable) {
        Mergeable<?> modelForMerge = lastSeenMergeableModel;
        lastSeenMergeableModel = (Mergeable<? extends BanditModel>) model.clone();
        if (modelForMerge != null) {
          model = ((Mergeable<BanditModel>) model).merge((BanditModel) modelForMerge);
        }
      }
      
      // update 
      armHits[model.update()]++;
      sumHits++;
      
      // model is updated based on the Global arm model => store it
      getModelHolder(0).add(model);
    }
  }
  
  public double[] getArmHits() {
    return armHits;
  }
  
  public double getSumHits() {
    return sumHits;
  }

}
