package gossipLearning.protocols;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.protocols.AbstractProtocol;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * This protocol uses multiple model holders, the number of model holder will be 
 * specified via the configuration file. Actually this protocol has the same number 
 * of model holders as the number of model names was set in the configuration.
 * 
 * @author István Hegedűs
 */
public class LearningProtocol extends AbstractProtocol {
  private static final String PAR_EXTRACTORPID = "extractorProtocol";
  protected static final String PAR_ARRGNAME = "aggrName";
  private static final String PAR_MODELHOLDERCAPACITY = "modelHolderCapacity";
  private static final String PAR_MODELHOLDERNAME = "modelHolderName";
  private static final String PAR_MODELNAMES = "modelNames";
  private static final String PAR_EVALNAMES = "evalNames";
  private static final String PAR_EVALPROB = "evalProbability";
  protected static final String PAR_MODELPROB = "initModelProbability";
  
  /**
   * Parameter name in the config file.
   * @hidden
   */
  public static final String PAR_WAIT = "numOfWaitingPeriods";
  /**
   * The number of periods without received model before send the current model.
   */
  protected final long numOfWaitingPeriods;
  protected long numberOfWaits;
  protected int numberOfIncomingModels;
  
  protected final int exrtactorProtocolID;
  
  protected ResultAggregator resultAggregator;
  protected final double evaluationProbability;
  protected final double initModelProbability;
  
  protected final int capacity;
  /** @hidden */
  protected final String modelHolderName;
  /** @hidden */
  protected final String[] modelNames;
  /** @hidden */
  protected ModelHolder[] modelHolders;
  /** @hidden */
  protected ModelHolder lastSeenMergeableModels;
  /** @hidden */
  protected final String[] evalNames;
  
  /**
   * Constructor which parses the content of a standard Peersim configuration file.
   *  
   * @param prefix
   */
  public LearningProtocol(String prefix) {
    this(prefix, Configuration.getInt(prefix + "." + PAR_MODELHOLDERCAPACITY));
  }
  
  protected LearningProtocol(String prefix, int capacity) {
    super(prefix);
    // loading configuration parameters
    exrtactorProtocolID = Configuration.getPid(prefix + "." + PAR_EXTRACTORPID);
    this.capacity = capacity;
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    modelNames = Configuration.getString(prefix + "." + PAR_MODELNAMES).split(",");
    evalNames = Configuration.getString(prefix + "." + PAR_EVALNAMES).split(",");
    evaluationProbability = Configuration.getDouble(prefix + "." + PAR_EVALPROB, 1.0);
    numOfWaitingPeriods = Configuration.getInt(prefix + "." + PAR_WAIT);
    initModelProbability = Configuration.getDouble(prefix + "." + PAR_MODELPROB, 1.0);
    
    // setting up learning related variables
    numberOfWaits = 0;
    numberOfIncomingModels = 1;
    if (CommonState.r.nextDouble() > initModelProbability) {
      numberOfIncomingModels = 0;
    }
    try {
      String aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
      resultAggregator = (ResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
      // holder for storing the last seen mergeable models for correct merge
      lastSeenMergeableModels = new BQModelHolder(modelNames.length);
      latestModelHolder = new BQModelHolder(modelNames.length);
      modelHolders = new ModelHolder[modelNames.length];
      for (int i = 0; i < modelNames.length; i++){
        try {
          modelHolders[i] = (ModelHolder)Class.forName(modelHolderName).getConstructor(int.class).newInstance(capacity);
        } catch (NoSuchMethodException e) {
          modelHolders[i] = (ModelHolder)Class.forName(modelHolderName).newInstance();
        }
        Model model = (Model)Class.forName(modelNames[i]).getConstructor(String.class).newInstance(prefix);
        lastSeenMergeableModels.add(model);
        modelHolders[i].add(model);
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }
  
  /**
   * Copy constructor.
   */
  protected LearningProtocol(LearningProtocol a) {
    super(a);
    exrtactorProtocolID = a.exrtactorProtocolID;
    capacity = a.capacity;
    modelHolderName = a.modelHolderName;
    modelNames = a.modelNames;
    evalNames = a.evalNames;
    evaluationProbability = a.evaluationProbability;
    numOfWaitingPeriods = a.numOfWaitingPeriods;
    initModelProbability = a.initModelProbability;
    
    numberOfWaits = a.numberOfWaits;
    numberOfIncomingModels = a.numberOfIncomingModels;
    
    resultAggregator = (ResultAggregator)a.resultAggregator.clone();
    lastSeenMergeableModels = (ModelHolder)a.lastSeenMergeableModels.clone();
    latestModelHolder = (ModelHolder)a.latestModelHolder.clone();
    modelHolders = new ModelHolder[a.modelHolders.length];
    for (int i = 0; i < modelHolders.length; i++) {
      modelHolders[i] = (ModelHolder)a.modelHolders[i].clone();
    }
  }
  
  /**
   * It produces a deep copy of the protocol.
   * 
   * @return Clone of the protocol instance.
   */
  @Override
  public Object clone() {
    return new LearningProtocol(this);
  }
  
  protected ModelHolder latestModelHolder;
  /**
   * It sends the latest models to a uniformly selected random neighbor.
   */
  @Override
  public void activeThread() {
    // evaluate
    for (int i = 0; i < modelHolders.length; i++) {
      if (CommonState.r.nextDouble() < evaluationProbability) {
        resultAggregator.push(currentProtocolID, i, modelHolders[i], ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getModel());
      }
    }
    
    // send
    if (numberOfIncomingModels == 0) {
      numberOfWaits ++;
    } else {
      numberOfWaits = 0;
    }
    if (numberOfWaits == numOfWaitingPeriods) {
      numberOfIncomingModels = 1;
      numberOfWaits = 0;
    }
    for (int id = Math.min(numberOfIncomingModels, capacity); id > 0; id --) {
      for (int i = 0; i < modelHolders.length; i++) {  
        // store the latest models in a new modelHolder
        Model latestModel = modelHolders[i].getModel(modelHolders[i].size() - id);
        latestModelHolder.add(latestModel);
      }
      if (latestModelHolder.size() == modelHolders.length) {
        // send the latest models to a random neighbor
        sendToRandomNeighbor(new ModelMessage(currentNode, latestModelHolder, currentProtocolID));
      }
      latestModelHolder.clear();
    }
    numberOfIncomingModels = 0;
  }

  /**
   * It processes an incoming modelHolder by updating and storing them with all of the
   * stored instances.
   */
  @Override
  public void passiveThread(ModelMessage message) {
    numberOfIncomingModels ++;
    if (message.getTargetPid() == currentProtocolID) {
      updateModels(message);
    }
  }
  
  /**
   * Updates the models of the specified model holder and merges them if it is possible.
   * @param modelHolder container of models to update
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void updateModels(ModelHolder modelHolder){
    // get instances from the extraction protocol
    InstanceHolder instances = ((ExtractionProtocol)currentNode.getProtocol(exrtactorProtocolID)).getInstances();
    for (int i = 0; i < modelHolder.size(); i++){
      // get the ith model from the modelHolder
      LearningModel model = (LearningModel)modelHolder.getModel(i);
      // if it is a mergeable model, then merge them
      if (model instanceof Mergeable){
        LearningModel lastSeen = (LearningModel)lastSeenMergeableModels.getModel(i);
        lastSeenMergeableModels.setModel(i, (LearningModel) model.clone());
        model = (LearningModel)((Mergeable) model).merge(lastSeen);
      }
      // updating the model with the local training samples
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        model.update(x, y);
      }
      // stores the updated model
      modelHolders[i].add(model);
    }
  }
  
  /**
   * Returns the aggregated result aggregate of this node.
   * @return the aggregated result.
   */
  public ResultAggregator getResults() {
    return resultAggregator;
  }
  
  /**
   * Sets the specified number of classes for the models.
   * @param numberOfClasses the number of classes to be set
   */
  public void setNumberOfClasses(int numberOfClasses) {
    for (int i = 0; i < modelHolders.length; i++) {
      for (int j = 0; j < modelHolders[i].size(); j++) {
        ((LearningModel)modelHolders[i].getModel(j)).setNumberOfClasses(numberOfClasses);
      }
    }
    for (int i = 0; i < lastSeenMergeableModels.size(); i++) {
      ((LearningModel)lastSeenMergeableModels.getModel(i)).setNumberOfClasses(numberOfClasses);
    }
  }
  
}