package gossipLearning.interfaces;

import java.util.Queue;

public interface ModelQueueHolder<M extends Model<?>> {
  public Queue<M> getModelQueue();
  public int getMemorySize();
}
