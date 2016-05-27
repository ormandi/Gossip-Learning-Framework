package gossipLearning.interfaces.protocols;

import gossipLearning.evaluators.ResultAggregator;

public interface InstanceLoaderConnection {
  public ResultAggregator getResults();
  public void setNumberOfClasses(int numberOfClasses);
}
