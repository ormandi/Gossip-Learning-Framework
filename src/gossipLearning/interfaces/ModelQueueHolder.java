package gossipLearning.interfaces;

import java.util.Queue;

public interface ModelQueueHolder<M extends Model<?>> {
  public void initModel();
  public Queue<M> getModelQueue();
  public int getMemorySize();
}
