package gossipLearning.main.fedAVG;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.InstanceHolder;

public class ModelUpdateTask implements Runnable {
  public final LearningModel model;
  public final InstanceHolder instances;
  public final int E;
  public final int B;
  public ModelUpdateTask(LearningModel model, InstanceHolder instances, int E, int B) {
    this.model = model;
    this.instances = instances;
    this.E = E;
    this.B = B;
  }
  @Override
  public void run() {
    model.update(instances, E, B);
  }
}
