package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.FeatureExtractorModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.protocols.AbstractProtocol;
import gossipLearning.messages.ModelMessage;
import gossipLearning.overlays.TMan;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.NodeDescriptor;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This is a feature extractor/manipulator protocol, that can learn 
 * a feature extractor model (for e.g. normalization) or use a static model
 * (for e.g. polynomial features).
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>modelName - the name of the extractor model</li>
 * <li>modelName.param - parameters of the extractor model</li>
 * <li>modelHolderName - the name of the model holder</li>
 * <li>modelHolderCapacity - the capacity of the model holder</li>
 * </ul>
 * @author István Hegedűs
 */
public class ExtractionProtocol extends AbstractProtocol {
  private static final String PAR_MODELNAMES = "modelName";
  private static final String PAR_MODELHOLDERNAME = "modelHolderName";
  private static final String PAR_MODELHOLDERCAPACITY = "modelHolderCapacity";
  private static final String PAR_ISUSETMAN = "isUseTMan";
  
  protected final int capacity;
  /** @hidden */
  protected final String modelHolderName;
  /** @hidden */
  protected final String modelName;
  protected ModelHolder modelHolder;
  /** @hidden */
  protected ModelHolder lastSeenMergeableModels;
  protected boolean isUseTMan;
  protected NodeDescriptor descriptor;
  
  protected InstanceHolder instances;
  
  /**
   * Constructor which parses the content of a standard Peersim configuration file.
   *  
   * @param prefix
   */
  public ExtractionProtocol(String prefix) {
    capacity = Configuration.getInt(prefix + "." + PAR_MODELHOLDERCAPACITY);
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAMES);
    isUseTMan = Configuration.getBoolean(prefix + "." + PAR_ISUSETMAN);
    descriptor = null;
    init(prefix);
  }
  
  /**
   * Copy constructor.
   * 
   * @param a to be copied
   */
  protected ExtractionProtocol(ExtractionProtocol a) {
    prefix = a.prefix;
    delayMean = a.delayMean;
    delayVar = a.delayVar;
    capacity = a.capacity;
    modelHolderName = a.modelHolderName;
    modelName = a.modelName;
    isUseTMan = a.isUseTMan;
    if (a.descriptor != null) {
      descriptor = (NodeDescriptor)a.descriptor.clone();
    }
    init(prefix);
  }
  
  /**
   * It initializes the starting modelHolder and model structure.
   * 
   * @param prefix
   */
  protected void init(String prefix) {
    try {
      super.init(prefix);
      // holder for storing the last seen mergeable models for correct merge
      lastSeenMergeableModels = new BQModelHolder(1);
      try {
        modelHolder = (ModelHolder)Class.forName(modelHolderName).getConstructor(int.class).newInstance(capacity);
      } catch (NoSuchMethodException e) {
        modelHolder = (ModelHolder)Class.forName(modelHolderName).newInstance();
      }
      Model model = (Model)Class.forName(modelName).newInstance();
      model.init(prefix);
      modelHolder.add(model);
      lastSeenMergeableModels.add(model);
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
    return new ExtractionProtocol(this);
  }
  
  /**
   * It sends the latest models to a uniformly selected random neighbor.
   */
  @Override
  public void activeThread() {
    ModelHolder latestModelHolder = new BQModelHolder(1);
    // store the latest models in a new modelHolder
    Model latestModel = modelHolder.getModel(modelHolder.size() - 1);
    latestModelHolder.add(latestModel);
    
    // send the latest models to a random neighbor
    sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID));
    
    // initialize or update descriptor
    SparseVector v = new SparseVector();
    for (int i = 0; i < instances.size(); i++) {
      v.add(instances.getInstance(i), 1.0 / instances.size());
    }
    if (descriptor == null) {
      descriptor = new NodeDescriptor(currentNode, v);
    } else {
      descriptor.setDecriptor(v);
    }
    descriptor.setSimilarity(descriptor.computeSimilarity(descriptor));
    if (isUseTMan) {
      ((TMan)getOverlay()).setDescriptor(descriptor);
    }
  }

  /**
   * It processes an incoming modelHolder by updating and storing them with all of the
   * stored instances.
   */
  @Override
  public void passiveThread(ModelMessage message) {
    if (message.getTargetPid() == currentProtocolID) {
      updateModels(message);
    }
  }
  
  /**
   * Updates the models of the specified model holder and merges them if it is possible.
   * @param modelHolder container of models to update
   */
  protected void updateModels(ModelHolder modelHolder){
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      Model m = modelHolder.getModel(i);
      if (m instanceof FeatureExtractorModel) {
        FeatureExtractorModel model = (FeatureExtractorModel)m;
        for (int index = 0; index < instances.size(); index ++) {
          // updating the model with the local training samples
          model.update(instances.getInstance(index));
        }
        // stores the updated model
        modelHolder.add(model);
      }
    }
  }
  
  /**
   * Returns the instances that have the extracted features.
   * @return the extracted instances
   */
  public InstanceHolder getInstances() {
    return getModel().extract(instances);
  }
  
  /**
   * Returns the extractor model that can be used for extracting features.
   * @return the extractor model
   */
  public FeatureExtractor getModel() {
    return (FeatureExtractor)modelHolder.getModel(modelHolder.size() - 1);
  }
  
  /**
   * Returns the reference for the raw instances of the protocol.
   * @return instances
   */
  public InstanceHolder getInstanceHolder() {
    return instances;
  }
  
  /**
   * Sets the reference of raw instances for the protocol.
   * @param instances to be set
   */
  public void setInstanceHolder(InstanceHolder instances) {
    this.instances = instances;
  }
  
}