package gossipLearning.interfaces.models;

public interface Partitioned extends Model {
  /**
   * Returns the model that contains the specified partition of the current model.
   * @param p ID of partition
   * @return a part of the model
   */
  public Partitioned getModelPart(int p);
}
