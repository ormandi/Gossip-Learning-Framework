package gossipLearning.interfaces.models;

/**
 * This interface describes the models that have an extra property, 
 * namely in this case a model can merge or combine itself with an other model.
 * 
 * @author Istvan Hegedus
 * @param <T> the type of the model that can be merged.
 */
public interface Mergeable<T extends Model> {
  
  /**
   * Returns the merged or the combined model of the actual and the specified models.
   * @note Recommended usage: merge the specified model to the current model and return this.
   * @param model the model that will be merged with the actual model
   * @return combined or merged model
   */
  public T merge(T model);
}
