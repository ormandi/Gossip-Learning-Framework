package gossipLearning.evaluators;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.SparseVector;

public abstract class FactorizationResultAggregator extends ResultAggregator {
  private static final long serialVersionUID = -3510139767870197602L;
  
  public FactorizationResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  
  protected FactorizationResultAggregator(FactorizationResultAggregator a) {
    super(a);
  }
  
  public abstract void push(int pid, int index, int userIdx, SparseVector userModel, ModelHolder modelHolder, FeatureExtractor extractor);

}
