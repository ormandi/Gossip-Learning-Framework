package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.interfaces.models.DenseCompressibleModel;
import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.VectorEntry;

/**
 * DenseCompressible version of the logistic regression classifier.
 * Bias is also compressed.
 */
public class DenseCompressibleLogReg extends MergeableLogReg implements DenseCompressibleModel {

  public DenseCompressibleLogReg(String prefix) {
    super(prefix);
  }
  
  /**
   * Returns a new DenseCompressibleLogReg object that initializes its variables with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected DenseCompressibleLogReg(DenseCompressibleLogReg a) {
    super(a);
  }
  
  @Override
  public DenseCompressibleLogReg clone() {
    return new DenseCompressibleLogReg(this);
  }
  
  @Override
  public double[] getData() {
    double[] data = new double[numberOfFeatures+1];
    for (VectorEntry e : w)
      data[e.index] = e.value;
    data[numberOfFeatures] = bias;
    return data;
  }
  
  @Override
  public void setData(double[] data) {
    w.clear();
    gradient.clear();
    for (int i=0; i<data.length-1; i++)
      w.add(i,data[i]);
    bias = data[data.length-1];
  }

}
