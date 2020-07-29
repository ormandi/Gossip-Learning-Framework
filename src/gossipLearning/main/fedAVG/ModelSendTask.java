package gossipLearning.main.fedAVG;

import java.util.Random;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Partializable;

public class ModelSendTask implements Runnable {
  public final LearningModel localModel;
  public final LearningModel globalModel;
  public final Random r;
  public final boolean downSlim;
  
  public ModelSendTask(LearningModel localModel, LearningModel globalModel, Random r, boolean downSlim) {
    this.localModel = localModel;
    this.globalModel = globalModel;
    this.r = r;
    this.downSlim = downSlim;
  }

  @Override
  public void run() {
    if (downSlim && globalModel instanceof Partializable) {
      localModel.set(((Partializable)globalModel).getModelPart(r));
    } else {
      localModel.set(globalModel);
    }
  }

}
