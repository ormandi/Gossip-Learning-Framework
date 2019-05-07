package gossipLearning.main.fedAVG;

import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.SparseVector;

public class RecSysModelUpdateTask implements Runnable {
  public final MatrixBasedModel model;
  public double[] rowModel;
  public final SparseVector instance;
  public RecSysModelUpdateTask(MatrixBasedModel model, double[] rowModel, SparseVector instance) {
    this.model = model;
    this.rowModel = rowModel;
    this.instance = instance;
  }
  @Override
  public void run() {
    this.rowModel = model.update(rowModel, instance);
  }
}
