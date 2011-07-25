package gossipLearning.interfaces;

public interface ModelHolder<M extends Model<?>> {
  public void initModel();
  public M getModel();
  public void setModel(M m);
}
