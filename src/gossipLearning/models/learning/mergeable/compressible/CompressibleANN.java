package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.models.learning.mergeable.MergeableANN;
import gossipLearning.interfaces.models.CompressibleModel;
import gossipLearning.utils.Matrix;
import java.util.Map;

/**
 * Compressible version of the artificial neural network classifier.
 */
public class CompressibleANN extends MergeableANN implements CompressibleModel {
  
  public CompressibleANN(String prefix) {
    super(prefix);
  }
  
  /**
   * Returns a new CompressibleANN object that initializes its variables with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected CompressibleANN(CompressibleANN a) {
    super(a);
  }
  
  @Override
  public CompressibleANN clone() {
    return new CompressibleANN(this);
  }
  
  @Override
  public void getData(Map<Integer,Double> map) {
    int ind = 0;
    for (Matrix theta : thetas)
      for (int i=0; i<theta.getRowDimension(); i++)
        for (int j=0; j<theta.getColumnDimension(); j++)
          map.put(ind++,theta.get(i,j));
  }
  
  @Override
  public void setData(Map<Integer,Double> map) {
    int ind = 0;
    for (Matrix theta : thetas)
      for (int i=0; i<theta.getRowDimension(); i++)
        for (int j=0; j<theta.getColumnDimension(); j++)
          theta.set(i,j,map.get(ind++));
  }

}
