package gossipLearning.evaluators;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.VectorEntry;

public class RecSysResultAggregator extends FactorizationResultAggregator {
  private static final long serialVersionUID = 7010094375422981942L;
  
  public RecSysResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  
  protected RecSysResultAggregator(RecSysResultAggregator a) {
    super(a);
  }
  
  @Override
  public RecSysResultAggregator clone() {
    return new RecSysResultAggregator(this);
  }
  
  @Override
  public void push(int pid, int index, int userIdx, double[] userModel, MatrixBasedModel model, FeatureExtractor extractor) {
    InstanceHolder eval = extractor.extract(evalSet);
    modelAges[index] = model.getAge();
    for (VectorEntry entry : eval.getInstance(userIdx)) {
      double expected = entry.value;
      double predicted = model.predict(userModel, entry.index);
      for (int j = 0; j < evaluators[index].length; j++) {
        if (evaluators[index][j] instanceof MatrixBasedEvaluator) {
          evaluators[index][j].evaluate(expected, Math.round(predicted));
        } else {
          evaluators[index][j].evaluate(expected, predicted);
        }
      }
    }
    push(pid, index);
  }

}
