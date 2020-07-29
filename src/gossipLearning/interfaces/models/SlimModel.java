package gossipLearning.interfaces.models;

public interface SlimModel extends Mergeable, Partializable {
  public Model weightedAdd(Model model, double times);
  public Model scale(double value);
  public double getSize();
}
