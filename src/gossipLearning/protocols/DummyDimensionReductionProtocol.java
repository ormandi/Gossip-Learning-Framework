package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.protocols.DimensionReductionProtocol;
import gossipLearning.utils.InstanceHolder;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class DummyDimensionReductionProtocol implements DimensionReductionProtocol,EDProtocol{

  private static final String PAR_MODELNAMES = "modelName";
  private static final String PAR_MODELHOLDERNAME = "modelHolderName";
  private static final String PAR_MODELHOLDERCAPACITY = "modelHolderCapacity";
  protected final int capacity;
  protected final String modelHolderName;
  protected final String modelName;
  protected InstanceHolder instances;
  protected ModelHolder modelHolder;

  public DummyDimensionReductionProtocol(String prefix) {
    capacity = Configuration.getInt(prefix + "." + PAR_MODELHOLDERCAPACITY);
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    modelName = Configuration.getString(prefix + "." + PAR_MODELNAMES);
    try {
      try {
        modelHolder = (ModelHolder)Class.forName(modelHolderName).getConstructor(int.class).newInstance(capacity);
      } catch (NoSuchMethodException e) {
        modelHolder = (ModelHolder)Class.forName(modelHolderName).newInstance();
      }
      Model model = (Model)Class.forName(modelName).getConstructor(String.class).newInstance(prefix);
      modelHolder.add(model);
    } catch (Exception e) {
      throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": ", e);
    }
  }

  public DummyDimensionReductionProtocol(DummyDimensionReductionProtocol a) {
    capacity = a.capacity;
    modelHolderName = a.modelHolderName;
    modelName = a.modelName;
    modelHolder = (ModelHolder) a.modelHolder.clone(true);
  }

  @Override
  public Object clone(){
    return new DummyDimensionReductionProtocol(this);
  }
  
  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {}
  
  
  @Override
  public InstanceHolder getInstances() {
    return getModel().extract(instances);
  }

  @Override
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
