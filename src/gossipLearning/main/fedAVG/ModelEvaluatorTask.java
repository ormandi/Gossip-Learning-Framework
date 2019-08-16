package gossipLearning.main.fedAVG;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.LearningModel;

public class ModelEvaluatorTask implements Runnable {
  public final ResultAggregator resultAggregator;
  public final LearningModel model;
  public final int pid;
  public final int idx;
  public ModelEvaluatorTask (ResultAggregator resultAggregator, LearningModel model, int pid, int idx) {
    this.resultAggregator = resultAggregator;
    this.model = model;
    this.pid = pid;
    this.idx = idx;
  }

  @Override
  public void run() {
    resultAggregator.push(pid, idx, model);
  }

}
