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
  /**
   * Adds the specified model to the current model.
   * @param model to be added
   * @return summed models
   */
  public Model add(Model model);
  /**
   * Adds the specified model to the current model scaled by the specified scale.
   * @param model to be added
   * @param scale scaling value
   * @return summed model
   */
  public Model add(Model model, double scale);
}
