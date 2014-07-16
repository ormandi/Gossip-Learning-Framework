package gossipLearning.models.extraction;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

/**
 * This class represents a dummy feature extractor that do nothing with the features.
 * 
 * @author István Hegedűs
 */
public class DummyExtractor implements FeatureExtractor {
  private static final long serialVersionUID = -5728157327626898691L;

  public DummyExtractor(String prefix) {
  }
  
  public Object clone() {
    return new DummyExtractor("");
  }
  
  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    return instances;
  }

  @Override
  public SparseVector extract(SparseVector instance) {
    return instance;
  }

  @Override
  public double getAge() {
    return 0.0;
  }

}
