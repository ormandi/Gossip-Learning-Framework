package gossipLearning.evaluators;

import java.util.Map;
import java.util.TreeMap;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

public class DummyFactorizationResultAggregator extends FactorizationResultAggregator {
  private static final long serialVersionUID = -8883339773613587699L;
  protected static Matrix UST;
  protected static Matrix VT;
  protected static Matrix S;
  protected static Map<Integer, Matrix[]> pid2US;
  protected static Map<Integer, double[][]> pid2USTUSp;
  
  public DummyFactorizationResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
    pid2US = new TreeMap<Integer, Matrix[]>();
    pid2USTUSp = new TreeMap<Integer, double[][]>();
  }
  
  protected DummyFactorizationResultAggregator(DummyFactorizationResultAggregator a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new DummyFactorizationResultAggregator(this);
  }
  
  @Override
  public void push(int pid, int index, int userIdx, SparseVector userModel, ModelHolder modelHolder, FeatureExtractor extractor) {} 
}
