package gossipLearning.interfaces;


public interface InstanceHolder<I> {
  public I getInstance();
  public void setInstance(I instance);
  public double getLabel();
  public void setLabel(double label);
}
