package gossipLearning.protocols;

import gossipLearning.interfaces.AbstractProtocol;
import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;
import gossipLearning.modelHolders.BoundedModelHolder;

import java.util.Map;

import peersim.config.Configuration;

/**
 * This protocol uses multiple model holders, the number of model holder will be 
 * specified via the configuration file. Actually this protocol has the same number 
 * of model holders as the number of model names was set in the configuration.
 * @author István Hegedűs
 *
 */
public class MultipleLearningProtocol extends AbstractProtocol {
  private static final String PAR_DELAYMEAN = "delayMean";
  private static final String PAR_DELAYVAR = "delayVar";
  private static final String PAR_MODELHOLDERNAME = "modelHolderName";
  private static final String PAR_MODELNAMES = "modelNames";
  
  /** @hidden */
  private final String modelHolderName;
  /** @hidden */
  private final String[] modelNames;
  /** @hidden */
  private final String prefix;
  /** @hidden */
  private ModelHolder[] modelHolders;
  /** @hidden */
  private ModelHolder lastSeenMergeableModels;
  
  /**
   * Constructor which parses the content of a standard Peersim configuration file.
   *  
   * @param prefix
   */
  public MultipleLearningProtocol(String prefix) {
    this.prefix = prefix;
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 1.0);
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    modelNames = Configuration.getString(prefix + "." + PAR_MODELNAMES).split(",");
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
  private MultipleLearningProtocol(String prefix, double delayMean, double delayVar, String modelHolderName, String[] modelNames) {
    this.prefix = prefix;
    this.delayMean = delayMean;
    this.delayVar = delayVar;
    this.modelHolderName = modelHolderName;
    this.modelNames = modelNames;
    init(prefix);
  }
  
  /**
   * It initializes the starting modelHolder and model structure.
   * 
   * @param prefix
   */
  private void init(String prefix) {
    try {
      // holder for storing the last seen mergeable models for correct merge
      lastSeenMergeableModels = new BoundedModelHolder(modelNames.length);
      modelHolders = new ModelHolder[modelNames.length];
      for (int i = 0; i < modelNames.length; i++){
        modelHolders[i] = (ModelHolder)Class.forName(modelHolderName).newInstance();
        modelHolders[i].init(prefix);
        Model model = (Model)Class.forName(modelNames[i]).newInstance();
        model.init(prefix);
        lastSeenMergeableModels.add(model);
        modelHolders[i].add(model);
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }
  
  /**
   * It produces a deep copy of the protocol.
   * 
   * @return Clone of the protocol instance.
   */
  @Override
  public Object clone() {
    return new MultipleLearningProtocol(prefix, delayMean, delayVar, modelHolderName, modelNames);
  }
  
  /**
   * It sends the latest models to a uniformly selected random neighbor.
   */
  @Override
  public void activeThread() {
    ModelHolder latestModelHolder = new BoundedModelHolder(modelHolders.length);
    for (int i = 0; i < modelHolders.length; i++) {  
      // store the latest models in a new modelHolder
      Model latestModel = modelHolders[i].getModel(modelHolders[i].size() - 1);
      latestModelHolder.add(latestModel);
    }
    
    // send the latest models to a random neighbor
    sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder));
  }

  /**
   * It processes an incoming modelHolder by updating and storing them with all of the
   * stored instances.
   */
  @Override
  public void passiveThread(ModelMessage message) {
    updateModels(message);
  }
  
  /**
   * Updates the models of the specified model holder and merges them if it is possible.
   * @param modelHolder container of models to update
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void updateModels(ModelHolder modelHolder){
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      Model model = modelHolder.getModel(i);
      // if it is a mergeable model, them merge them
      if (model instanceof Mergeable){
        Model lastSeen = lastSeenMergeableModels.getModel(i);
        lastSeenMergeableModels.setModel(i, (Model) model.clone());
        model = ((Mergeable) model).merge(lastSeen);
      }
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        Map<Integer, Double> x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        model.update(x, y);
      }
      // stores the updated model
      //modelHolders[i].add((Model) model.clone());
      modelHolders[i].add(model);
    }
  }

  /**
   * Returns the number of handled model holders.
   * 
   * @return the number of model holders
   */
  @Override
  public int size() {
    return modelHolders.length;
  }

  /**
   * It returns the model holder at the specified index, if there is no model at 
   * index then it throws an exception.
   * 
   * @param index index of model holder to return
   * @return model holder at index
   */
  @Override
  public ModelHolder getModelHolder(int index) {
    if (index >= modelHolders.length || index < 0) {
      throw new RuntimeException(getClass().getCanonicalName() + " has not enough modelholders!", new IndexOutOfBoundsException("index: " + index));
    }
    return modelHolders[index];
  }

  /**
   * Sets the specified model holder at the specified index.
   * @param index index of model holder to set
   * @param modelHolder model holder to be set
   */
  @Override
  public void setModelHolder(int index, ModelHolder modelHolder) {
    if (index >= modelHolders.length || index < 0) {
      throw new RuntimeException(getClass().getCanonicalName() + " has not enough modelholders!", new IndexOutOfBoundsException("index: " + index));
    }
    modelHolders[index] = modelHolder;
  }

  /**
   * Not able to add new model holder!
   */
  @Override
  public boolean add(ModelHolder modelHolder) {
    return false;
  }

  /**
   * Not able to remove model holder!
   */
  @Override
  public ModelHolder remove(int index) {
    return null;
  }

  
}