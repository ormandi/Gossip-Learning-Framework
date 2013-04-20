package gossipLearning.evaluators;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class RecSysResultAggregator extends ResultAggregator {
  private static final long serialVersionUID = 7010094375422981942L;
  
  public RecSysResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  
  public void push(int pid, int index, int userIdx, SparseVector userModel, ModelHolder modelHolder, FeatureExtractor extractor) {
    if (modelHolder.size() == 0) {
      return;
    }
    InstanceHolder eval = extractor.extract(evalSet);
    MatrixBasedModel model = (MatrixBasedModel)modelHolder.getModel(modelHolder.size() - 1);
    modelAges[index] = model.getAge();
    for (VectorEntry entry : eval.getInstance(userIdx)) {
      double expected = entry.value;
      double predicted = model.predict(userIdx, userModel, entry.index);
      for (int j = 0; j < evaluators[index].length; j++) {
        evaluators[index][j].evaluate(expected, predicted);
      }
    }
    push(pid, index);
  }

}
