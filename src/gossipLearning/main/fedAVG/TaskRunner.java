package gossipLearning.main.fedAVG;

import java.util.LinkedList;

public class TaskRunner {
  private final LinkedList<Runnable> queue;
  private final Worker[] workers;
  public TaskRunner(int numThreads) {
    queue = new LinkedList<Runnable>();
    workers = new Worker[numThreads];
  }
  
  public void add(Runnable task) {
    queue.add(task);
  }
  
  private synchronized Runnable get() {
    return queue.poll();
  }
  
  public void run() throws Exception {
    for (int i = 0; i < workers.length; i++) {
      workers[i] = new Worker(this);
      workers[i].start();
    }
    for (int i = 0; i < workers.length; i++) {
      workers[i].join();
    }
  }
  
  private class Worker extends Thread {
    private final TaskRunner container;
    public Worker(TaskRunner container) {
      this.container = container;
    }
    public void run() {
      Runnable task;
      while((task = container.get()) != null) {
        task.run();
      }
    }
  }
  
}
