package gossipLearning.main.fedAVG;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;

public class ModelSumTask implements Runnable {

  private final Model sum;
  private final Model globalModel;
  private final Model[] list;
  private final int from;
  private final int to;
  private final double scale;
  private final boolean[] isOnline;
  private final long[] sessionEnd;
  private final int t;
  private final long delay;
  
  public ModelSumTask(Model sum, Model globalModel, Model[] list, int from, int to, double scale, boolean[] isOnline, long[] sessionEnd, int t, long delay) {
    this.sum = sum;
    this.globalModel = globalModel;
    this.list = list;
    this.from = from;
    this.to = to;
    this.scale = scale;
    this.isOnline = isOnline;
    this.sessionEnd = sessionEnd;
    this.t = t;
    this.delay = delay;
  }
  @Override
  public void run() {
    for (int i = from; i < to; i++) {
      if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
        continue;
      }
      //((Addable)sum).add(((Partializable)((Addable)list[i]).add(globalModel, -1)).getModelPart(), scale);
      ((Addable)sum).add(((Partializable)list[i]).getModelPart(), scale);
    }
  }
}
