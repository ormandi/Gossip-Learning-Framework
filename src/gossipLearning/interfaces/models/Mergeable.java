package gossipLearning.interfaces.models;

/**
 * This interface describes the models that have an extra property, 
 * namely in this case a model can merge or combine itself with an other model.
 * 
 * @author Istvan Hegedus
 */
public interface Mergeable {
  
  /**
   * Merges the specified model into the current model.
   * @param model the model that will be merged with the actual model
   * @return combined or merged model
   */
  public Model merge(Model model);
}
