package gossipLearning.interfaces.models;

public interface Addable {
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
