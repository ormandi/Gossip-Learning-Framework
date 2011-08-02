package gossipLearning.interfaces;

import java.util.Queue;

public interface ModelQueueHolder<I> {
  public void initModel();
  public Queue<Model<I>> getModelQueue();
  public int getMemorySize();
}
