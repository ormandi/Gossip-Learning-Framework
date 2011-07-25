package gossipLearning.interfaces;


public interface InstancesHolder<I> {
  public I getInstance(int i);
  public void setInstance(int i, I instance);
  public double getLabel(int i);
  public void setLabel(int i, double label);
  public int numberOfInstances();
  public void setNumberOfInstances(int n);

}
