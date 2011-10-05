package gossipLearning.protocols;

import gossipLearning.InstanceHolder;
import gossipLearning.controls.ChurnControl;
import gossipLearning.interfaces.AbstractProtocol;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import peersim.config.Configuration;
import peersim.core.Node;

/**
 * This is a simple LearningProtocol which handles only one modelHolder with
 * a fixed size. This modelHolder works like a LIFO i.e. when it is full and a 
 * new model arrives the oldest one is removed.<br/>
 * The active behavior of the protocol is also as simple as possible, it simply
 * select a random node and send the latest model from its ModelHolder.<br/>
 * At the passive thread when a model is received first it will be updated
 * via <b>all</b> of the training smaples contained by the node, then it will be
 * added to the ModelHolder of the node. 
 * 
 * @author Róbert Ormándi
 *
 */
public class SimpleLearningProtocol extends AbstractProtocol {
  protected static final String PAR_DELAYMEAN = "delayMean";
  protected static final String PAR_DELAYVAR = "delayVar";

  private ModelHolder models;
  
  /**
   * Constructor which parses the content of a standard Peersim config file.
   *  
   * @param prefix
   */
  public SimpleLearningProtocol(String prefix) {
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 1.0);    
  }
  
  /**
   * Copy constructor.
   * 
   * @param delayMean
   * @param delayVar
   * @param instances
   * @param sessionLength
   * @param sessionID
   * @param currentNode
   * @param currentProtocolID
   * @param models
   */
  private SimpleLearningProtocol(double delayMean, double delayVar, InstanceHolder instances, long sessionLength, int sessionID, Node currentNode, int currentProtocolID, ModelHolder models) {
    this.delayMean = delayMean;
    this.delayVar = delayVar;
    this.instances = (InstanceHolder) instances.clone();
    this.sessionLength = sessionLength;
    this.sessionID = sessionID;
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    this.models = (ModelHolder)models.clone();
  }
  
  /**
   * It produces a deep copy of the protocol.
   * 
   * @return Clone of the protocol instance.
   */
  @Override
  public Object clone() {
    return new SimpleLearningProtocol(delayMean, delayVar, instances, sessionLength, sessionID, currentNode, currentProtocolID, models);
  }
  
  @Override
  public void activeThread() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void passiveThread(ModelMessage message) {
    // TODO Auto-generated method stub
    
  }

  /**
   * The size is allways 0 or 1 meaning that we have only zero or one ModelHolder instance.
   * 
   * @return The protocol handles only zero or one ModelHolder instance.
   */
  @Override
  public int size() {
    return (models == null) ? 0 : 1;
  }
  
  /**
   * It returns the only one stored ModelHolder instance if the index is 0,
   * otherwise throws an exception.
   * 
   * @param index Index which allways has to be 0.
   * @return The stored ModelHolder instance.
   */
  @Override
  public ModelHolder getModelHolder(int index) {
    if (index != 0) {
      throw new RuntimeException(getClass().getCanonicalName() + " can handle only one modelHolder with index 0.");
    }
    return models;
  }
  
  /**
   * It simply replaces the stored ModelHolder intance.
   * 
   * @param index Index which has to be 0.
   * @param modelHolder The new model holder.
   */
  @Override
  public void setModelHolder(int index, ModelHolder modelHolder) {
    if (index != 0) {
      throw new RuntimeException(getClass().getCanonicalName() + " can handle only one modelHolder with index 0.");
    }
    this.models = modelHolder;
  }

  /**
   * It overwrites the stored ModelHolder with the received one.
   * 
   *  @param New ModelHolder instance
   *  @return true The process is allways considered success.
   */
  @Override
  public boolean add(ModelHolder modelHolder) {
    setModelHolder(0, modelHolder);
    return true;
  }

  /**
   * It returns the stored ModelHolder and sets the current one to <i>null</i>.
   * 
   * @param It has to be 0.
   * @return ModelHolder instance which was stored by the node.
   */
  @Override
  public ModelHolder remove(int index) {
    if (index != 0) {
      throw new RuntimeException(getClass().getCanonicalName() + " can handle only one modelHolder with index 0.");
    }
    ModelHolder ret = models;
    models = null;
    return ret;
  }
}