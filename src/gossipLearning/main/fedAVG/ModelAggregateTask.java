package gossipLearning.main.fedAVG;

import java.util.List;
import java.util.Random;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;

public class ModelAggregateTask implements Runnable {
  public final LearningModel result;
  public final LearningModel[] localModels;
  public final LearningModel globalModel;
  public final Random[] r;
  public final double coef;
  public final List<Integer> indices;
  
  public ModelAggregateTask(LearningModel result, LearningModel[] localModels, LearningModel globalModel, Random[] r, double coef, List<Integer> indices) {
    this.result = result;
    this.localModels = localModels;
    this.globalModel = globalModel;
    this.r = r;
    this.coef = coef;
    this.indices = indices;
  }
  
  @Override
  public void run() {
    for (int idx : indices) {
      Model model = ((Addable)localModels[idx].clone()).add(globalModel, -1);
      if (model instanceof Partializable) {
        model = ((Partializable)model).getModelPart(r[idx]);
      }
      // averaging updated models
      if (result instanceof SlimModel) {
        ((SlimModel)result).weightedAdd(model, coef);
      } else {
        ((Addable)result).add(model, coef);
      }
    }
  }

}
