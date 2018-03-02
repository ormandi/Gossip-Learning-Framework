package gossipLearning.interfaces.models;

public interface Partializable extends Model {
  /**
   * Returns the model that contains the part of the current model.
   * @return a part of the model
   */
  public Model getModelPart();
}
