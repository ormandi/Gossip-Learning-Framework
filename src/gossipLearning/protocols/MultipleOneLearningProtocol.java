package gossipLearning.protocols;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Map;

import peersim.core.CommonState;

/**
 * This protocol uses multiple model holders, the number of model holder will be 
 * specified via the configuration file. Actually this protocol has the same number 
 * of model holders as the number of model names was set in the configuration. Moreover 
 * this protocol updates the models with <b>only one</b> randomly selected training sample from 
 * the training instance instead of the whole training set.
 * @author István Hegedűs
 *
 */
public class MultipleOneLearningProtocol extends MultipleLearningProtocol {
  
  /**
   * Constructor which parses the content of a standard Peersim configuration file.
   *  
   * @param prefix
   */
  public MultipleOneLearningProtocol(String prefix) {
    super(prefix);
  }
  
  /**
   * Copy constructor, makes a copy of the specified protocol using the copy constructor of 
   * the super class.
   * @param a protocol to copy
   */
  protected MultipleOneLearningProtocol(MultipleOneLearningProtocol a) {
    super(a.prefix, a.delayMean, a.delayVar, a.modelHolderName, a.modelNames);
  }
  
  /**
   * It produces a deep copy of the protocol.
   * 
   * @return Clone of the protocol instance.
   */
  @Override
  public Object clone() {
    return new MultipleOneLearningProtocol(this);
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
      // updating the model with only one randomly selected local training sample
      if (instances != null) {
        int sampleID = CommonState.r.nextInt(instances.size());
        Map<Integer, Double> x = instances.getInstance(sampleID);
        double y = instances.getLabel(sampleID);
        model.update(x, y);
      }
      // stores the updated model
      modelHolders[i].add(model);
    }
  }

}