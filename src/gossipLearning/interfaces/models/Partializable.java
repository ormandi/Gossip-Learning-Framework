package gossipLearning.interfaces.models;

import java.util.Set;

public interface Partializable<T extends Model> extends Model {
  /**
   * Returns the model that contains the part of the current model, that 
   * indices were specified.
   * @param indices the indices of the part of the current model
   * @return the specified part of the model
   */
  public T getModelPart(Set<Integer> indices);
}
