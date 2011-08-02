package gossipLearning.interfaces;

public interface Model<I> extends Cloneable {
  public double predict(I instance);
  public Object clone();
}


