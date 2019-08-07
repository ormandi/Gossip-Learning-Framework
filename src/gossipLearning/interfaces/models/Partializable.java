package gossipLearning.interfaces.models;

import java.util.Random;

public interface Partializable extends Model {
  /**
   * Returns the model that contains the part of the current model.
   * @return a part of the model
   */
  public Model getModelPart();
  /**
   * Returns the model that contains the part of the current model, 
   * using the specified random generator.
   * @param r used in the selection
   * @return a part of the model
   */
  public Model getModelPart(Random r);
}
