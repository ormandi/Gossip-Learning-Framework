package gossipLearning.models.extraction;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.InstanceHolder;

/**
 * This class represents a dummy feature extractor that do nothing with the features.
 * 
 * @author István Hegedűs
 */
public class DummyExtractor implements FeatureExtractor {
  private static final long serialVersionUID = -5728157327626898691L;

  public DummyExtractor() {
  }
  
  public Object clone() {
    return new DummyExtractor();
  }
  
  @Override
  public void init(String prefix) {    
  }
  
  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    return instances;
  }

  @Override
  public double getAge() {
    return 0.0;
  }

}
