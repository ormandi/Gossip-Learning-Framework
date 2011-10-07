package gossipLearning.protocols;

import gossipLearning.interfaces.AbstractProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;

import java.util.Map;

import peersim.config.Configuration;

/**
 * This is a simple LearningProtocol which handles only one modelHolder with
 * a fixed size (memory). This modelHolder works like a LIFO i.e. 
 * when it is full and new models arrive the oldest one is removed.<br/>
 * The active behavior of the protocol is also as simple as possible, it simply
 * select a random node and send the latest model from its ModelHolder.<br/>
 * At the passive thread when a model is received first it will be updated
 * via <b>all</b> of the training samples contained by the node, then it will be
 * added to the ModelHolder of the node.<br/>
 * The protocol receives three parameters in its constructor from the Peersim
 * configuration file:
 * <ul>
 *   <li><b>delayMean</b> - expected value of active thread delay (default value: Double.POSITIVE_INFINITY)</li>
 *   <li><b>delayVar</b> - variance of the delay of active thread (default value: 1.0)</li>
 *   <li><b>memorySize</b> - the bound parameter which defines at most 
 *   how many model can be stored in the ModelHolder instance of the protocol (default value: 1)</li>
 * </ul>
 * 
 * @author Róbert Ormándi
 *
 */
public class SimpleLearningProtocol extends AbstractProtocol {
  protected static final String PAR_DELAYMEAN = "delayMean";
  protected static final String PAR_DELAYVAR = "delayVar";
  protected static final String PAR_MODELHOLDERNAME = "modelHolderName";
  protected static final String PAR_MODELNAME = "modelName";
  
  private final String modelHolderName;
  private final String modelName;
  private final String prefix;
  
  private ModelHolder models;
  
  /**
   * Constructor which parses the content of a standard Peersim configuration file.
   *  
   * @param prefix
   */
  public SimpleLearningProtocol(String prefix) {
    this.prefix = prefix;
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 1.0);
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
    init(prefix);
  }
  
  /**
   * Copy constructor.
   * 
   * @param prefix
   * @param delayMean
   * @param delayVar
   * @param modelHolderName
   * @param modelName
   */
  private SimpleLearningProtocol(String prefix, double delayMean, double delayVar, String modelHolderName, String modelName) {
    this.prefix = prefix;
    this.delayMean = delayMean;
    this.delayVar = delayVar;
    this.modelHolderName = modelHolderName;
    this.modelName = modelName;
    init(prefix);
  }
  
  /**
   * It initializes the starting modelHolder and model structure.
   * 
   * @param prefix
   */
  private void init(String prefix) {
    try {
      models = (ModelHolder)Class.forName(modelHolderName).newInstance();
      models.init(prefix);
      Model model = (Model)Class.forName(modelName).newInstance();
      model.init(prefix);
      models.add(model);
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": " + e);
    }
  }
  
  /**
   * It produces a deep copy of the protocol.
   * 
   * @return Clone of the protocol instance.
   */
  @Override
  public Object clone() {
    return new SimpleLearningProtocol(prefix, delayMean, delayVar, modelHolderName, modelName);
  }
  
  /**
   * It sends the latest model to a uniformly selected random neighbor.
   */
  @Override
  public void activeThread() {
    // check whether the node has at least one model
    if (models != null && models.size() > 0){
      
      // store the latest model in a new modelHolder
      Model latestModel = models.getModel(models.size() - 1);
      ModelHolder latestModelHolder = new BoundedModelHolder(1);
      latestModelHolder.add(latestModel);
      
      // send the latest model to a random neighbor
      sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
    }
  }
  
  /**
   * It process an incoming modelHolder by updating them with all of
   * stored instances and storing them.
   */
  @Override
  public void passiveThread(ModelMessage message) {
    for (int incommingModelID = 0; message != null && incommingModelID < message.size(); incommingModelID ++) {
      // process each model that can be found in the message (they are clones, so not necessary to copy them again)
      Model model = message.getModel(incommingModelID);
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        Map<Integer, Double> x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        model.update(x, y);
      }
      // model is updated properly by all of the stored samples => store it
      models.add(model);
    }
  }

  /**
   * The size is always 0 or 1 meaning that we have only zero or one ModelHolder instance.
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
   * @param index Index which always has to be 0.
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
   * It simply replaces the stored ModelHolder instance.
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
   *  @return true The process is always considered success.
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