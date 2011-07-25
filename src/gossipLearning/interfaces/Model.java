package gossipLearning.interfaces;

import gossipLearning.utils.Cloneable;

public interface Model<I> extends Cloneable<Model<I>> {
  public double predict(I instance);
}


