package gossipLearning.evaluators;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

public class SamplingLowRankResultAggregator extends LowRankResultAggregator {
  private static final long serialVersionUID = -8883339773613587699L;
  
  public SamplingLowRankResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
  }
  
  protected SamplingLowRankResultAggregator(SamplingLowRankResultAggregator a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new SamplingLowRankResultAggregator(this);
  }
  
  @Override
  public void push(int pid, int index, int userIdx, SparseVector userModel, ModelHolder modelHolder, FeatureExtractor extractor) {
    if (modelHolder.size() == 0) {
      return;
    }
    MatrixBasedModel model = (MatrixBasedModel)modelHolder.getModel(modelHolder.size() - 1);
    modelAges[index] = model.getAge();
    Matrix v = model.getV();
    // the cosine similarity of the eigenvalues should be 1
    double expected = 1.0;
    if (v.getRowDimension() == VT.getColumnDimension()) {
      // The trace of the US^TSUp matrix should contain the square of the eigenvalues.
      // US^T is the expected left eigenvectors multiplied by the corresponding eigenvalues.
      // USp is the computed left eigenvectors multiplied by the corresponding eigenvalues.
      lock.lock();
      if (!pid2US.containsKey(pid)) {
        Matrix[] tmpM = new Matrix[modelNames.length];
        for (int i = 0; i < tmpM.length; i++) {
          tmpM[i] = new Matrix(UST.getColumnDimension(), v.getColumnDimension());
        }
        pid2US.put(pid, tmpM);
        double[][] tmpT = new double[modelNames.length][];
        for (int i = 0; i < tmpT.length; i++) {
          tmpT[i] = new double[UST.getRowDimension()];
        }
        pid2USTUSp.put(pid, tmpT);
      }
      Matrix USp = pid2US.get(pid)[index];
      double[] USTUSp = pid2USTUSp.get(pid)[index];
      
      //for (int idx = 0; idx < ResultAggregator.evalSet.size(); idx ++) {
      Matrix USi = v.mulLeft(ResultAggregator.evalSet.getInstance(userIdx));
      //System.out.println(userIdx +  " " + USi);
      //Matrix USi = model.getUSi(userModel);
      
      for (int i = 0; i < USi.getNumberOfColumns(); i++) {
        USTUSp[i] -= UST.get(i, userIdx) * USp.get(userIdx, i);
        USp.set(userIdx, i, USi.get(0, i));
        USTUSp[i] += UST.get(i, userIdx) * USp.get(userIdx, i);
        /*if (Double.isNaN(UST.get(i, userIdx) * USp.get(userIdx, i))) {
          System.out.println("NAN: " + UST.get(i, userIdx) + " " + USp.get(userIdx, i) + " " + USi.get(0, i));
          System.out.println(USi);
          System.exit(0);
        }*/
      }
      //}
      //Matrix USTUSp = UST.mul(USp);
      lock.unlock();
      
      
      // The trace of the V^Tv matrix should contain only 1s.
      // V^T is the expected right eigenvectors
      // v is the computed right eigenvectors
      //Matrix VTv = VT.mul(v);
      double[] VTvtr = new double[v.getColumnDimension()];
      for (int i = 0; i < Math.min(v.getColumnDimension(), v.getRowDimension()) && S.get(i, i) != 0.0; i++) {
        for (int k = 0; k < v.getRowDimension(); k++) {
          VTvtr[i] += VT.get(i,k) * v.get(k, i);
        }
        //double predicted = Math.abs(VTv.get(i, i));
        double predicted = Math.abs(VTvtr[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          //System.out.println("1: " + predicted);
          evaluators[index][j].evaluate(expected, predicted);
        }
        predicted = Math.abs(USTUSp[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          //System.out.println("2: " + predicted);
          //evaluators[index][j].evaluate(expected, predicted / (S.get(i, i)*S.get(i, i)));
        }
      }
    } else {
      for (int j = 0; j < evaluators[index].length; j++) {
        evaluators[index][j].evaluate(expected, 0.0);
      }
    }
    push(pid, index);
  }
  
}
