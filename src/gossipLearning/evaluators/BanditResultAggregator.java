package gossipLearning.evaluators;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;

import java.util.Iterator;

public class BanditResultAggregator extends ResultAggregator {
  private static final long serialVersionUID = -3609897125794071415L;
  public BanditResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  public void setEvalSet(InstanceHolder evalSet) {
  }
  public void push(int pid, int index, ModelHolder modelHolder, FeatureExtractor extractor) {
  }
  public Iterator<AggregationResult> iterator() {
    return null;
  }

}
