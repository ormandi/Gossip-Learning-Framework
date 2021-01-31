package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.models.learning.mergeable.MergeableOvsA;
import gossipLearning.interfaces.models.DenseCompressibleModel;
import java.util.*;

/**
 * A one-vs-all meta-classifier that contains DenseCompressibleModels.
 */
public class DenseCompressibleOvsA extends MergeableOvsA implements DenseCompressibleModel {

  public DenseCompressibleOvsA(String prefix) {
    super(prefix);
  }
  
  /** Copy constructor for deep copy. */
  public DenseCompressibleOvsA(DenseCompressibleOvsA a) {
    super(a);
  }
  
  @Override
  public DenseCompressibleOvsA clone() {
    return new DenseCompressibleOvsA(this);
  }
  
  @Override
  public double[] getData() {
    double[] data = null;
    for (int i=0; i<numberOfClasses; i++) {
      double[] m = ((DenseCompressibleModel)classifiers.getModel(i)).getData();
      if (i==0)
        data = new double[numberOfClasses*m.length];
      assert data.length==numberOfClasses*m.length; // models must have the same dimension
      System.arraycopy(m,0,data,i*m.length,m.length);
    }
    return data;
  }
  
  @Override
  public void setData(double[] data) {
    for (int i=0; i<numberOfClasses; i++)
      ((DenseCompressibleModel)classifiers.getModel(i)).setData(Arrays.copyOfRange(data,i*data.length/numberOfClasses,(i+1)*data.length/numberOfClasses));
  }
  
}
