package gossipLearning.evaluators;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;

import java.util.Iterator;

/**
 * Do nothing but necessary for fitting the bandit problem into the framework.
 * @author István Hegedűs
 */
public class BanditResultAggregator extends ResultAggregator {
  private static final long serialVersionUID = -3609897125794071415L;
  public BanditResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  protected BanditResultAggregator(BanditResultAggregator a) {
    super(a);
  }
  public Object clone() {
    return new BanditResultAggregator(this);
  }
  public void setEvalSet(InstanceHolder evalSet) {
  }
  public void push(int pid, int index, ModelHolder modelHolder, FeatureExtractor extractor) {
  }
  public Iterator<AggregationResult> iterator() {
    return null;
  }

}