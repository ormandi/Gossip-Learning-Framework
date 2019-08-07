package gossipLearning.protocols;

import gossipLearning.evaluators.FactorizationResultAggregator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class FactorizationProtocol extends LearningProtocol {
  /**
   * One user model for every model
   */
  protected double[][] userModels;
  
  public FactorizationProtocol(String prefix) {
    super(prefix);
    String aggrClassName = Configuration.getString(prefix + "." + PAR_ARRGNAME);
    try {
      resultAggregator = (FactorizationResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(modelNames, evalNames);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    userModels = new double[modelNames.length][];
  }
  
  protected FactorizationProtocol(FactorizationProtocol a) {
    super(a);
    resultAggregator = a.resultAggregator.clone();
    userModels = new double[a.userModels.length][];
    for (int i = 0; i < userModels.length; i++) {
      if (a.userModels[i] != null) {
        userModels[i] = a.userModels[i].clone();
      }
    }
  }
  
  @Override
  public FactorizationProtocol clone() {
    return new FactorizationProtocol(this);
  }
  
  @Override
  protected void evaluate() {
    for (int i = 0; i < models.length; i++) {
      ((FactorizationResultAggregator)resultAggregator).push(currentProtocolID, i, (int)currentNode.getID(), userModels[i], (MatrixBasedModel)models[i]);
    }
  }
  
  protected void updateModels(ModelHolder modelHolder) {
    // get instances from the extraction protocol
    if (instances.size() > 1) {
      throw new RuntimeException("The number of instances should be one at avery node instead of " + instances.size());
    }
    for (int i = 0; i < modelHolder.size(); i++){
      // get the last (only) model from the modelHolder
      MatrixBasedModel recvModel = (MatrixBasedModel)modelHolder.getModel(i);
      // merge if the model is capable
      if (recvModel instanceof Mergeable) {
        models[i] = (Model)((Mergeable) models[i]).merge(recvModel);
      } else {
        models[i] = recvModel;
      }
      // updating the model with the local training samples
      if (instances.size() != 1) {
        throw new RuntimeException("Num of instances has to be 1 instead of " + instances.size());
      }
      for (int sampleID = 0; instances != null && sampleID < instances.size(); sampleID ++) {
        // we use each samples for updating the currently processed model
        SparseVector x = instances.getInstance(sampleID);
        userModels[i] = ((MatrixBasedModel)models[i]).update(userModels[i], x);
      }
    }
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
  }

}
