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
  
  public ModelSumTask(Model sum, Model globalModel, Model[] list, int from, int to, double scale) {
    this.sum = sum;
    this.globalModel = globalModel;
    this.list = list;
    this.from = from;
    this.to = to;
    this.scale = scale;
  }
  @Override
  public void run() {
    for (int i = from; i < to; i++) {
      ((Addable)sum).add(((Partializable)((Addable)list[i]).add(globalModel, -1)).getModelPart(), scale);
    }
  }
}
