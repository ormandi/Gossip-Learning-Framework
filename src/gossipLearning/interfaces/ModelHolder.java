package gossipLearning.interfaces;

public interface ModelHolder<I> {
  public void initModel();
  public Model<I> getModel();
  public void setModel(Model<I> m);
}
