package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.models.recsys.LowRankDecomposition;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class RecSysResultAggregator extends ResultAggregator {
  private static final long serialVersionUID = 7010094375422981942L;
  
  public RecSysResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  
  public void push(int pid, int index, int userIdx, SparseVector userModel, ModelHolder modelHolder, FeatureExtractor extractor) {
    lock.lock();
    if (modelHolder.size() == 0) {
      return;
    }
    if (!pid2ModelNames.containsKey(pid)) {
      pid2ModelNames.put(pid, modelNames);
      pid2ModelAges.put(pid, new StringBuffer[modelAges.length]);
      pid2EvalNames.put(pid, evalNames);
    }
    InstanceHolder eval = extractor.extract(evalSet);
    LowRankDecomposition model = (LowRankDecomposition)modelHolder.getModel(modelHolder.size() - 1);
    modelAges[index] = model.getAge();
    StringBuffer[] buffs = pid2ModelAges.get(pid);
    if (AggregationResult.isPrintAges) {
      buffs[index].append(' ');
      buffs[index].append(modelAges[index]);
    }
    
    Evaluator[][] evaluator = aggregations.get(pid);
    if (evaluator == null) {
      evaluator = evaluators;
      aggregations.put(pid, evaluator);
    }
    
    for (VectorEntry entry : eval.getInstance(userIdx)) {
      double expected = entry.value;
      double predicted = model.predict(userIdx, userModel, entry.index);
      for (int j = 0; j < evaluator[index].length; j++) {
        evaluator[index][j].evaluate(expected, predicted);
      }
    }
    lock.unlock();
  }

}
