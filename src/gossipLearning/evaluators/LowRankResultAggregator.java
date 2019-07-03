package gossipLearning.evaluators;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.VectorEntry;
import gossipLearning.utils.jama.SingularValueDecomposition;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class LowRankResultAggregator extends FactorizationResultAggregator {
  private static final long serialVersionUID = -8883339773613587699L;
  protected static Matrix UST;
  protected static Matrix VT;
  protected static Matrix S;
  protected static Map<Integer, Matrix[]> pid2US;
  protected static Map<Integer, double[][]> pid2USTUSp;
  
  public LowRankResultAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
    pid2US = new TreeMap<Integer, Matrix[]>();
    pid2USTUSp = new TreeMap<Integer, double[][]>();
  }
  
  protected LowRankResultAggregator(LowRankResultAggregator a) {
    super(a);
  }
  
  @Override
  public LowRankResultAggregator clone() {
    return new LowRankResultAggregator(this);
  }
  
  @Override
  public void push(int pid, int index, int userIdx, double[] userModel, MatrixBasedModel model, FeatureExtractor extractor) {
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
      Matrix USi = model.getUSi(userModel);
      for (int i = 0; i < USi.getNumberOfColumns(); i++) {
        USTUSp[i] -= UST.get(i, userIdx) * USp.get(userIdx, i);
        USp.set(userIdx, i, USi.get(0, i));
        USTUSp[i] += UST.get(i, userIdx) * USp.get(userIdx, i);
      }
      lock.unlock();
      
      
      // The trace of the V^Tv matrix should contain only 1s.
      // V^T is the expected right eigenvectors
      // v is the computed right eigenvectors
      double[] VTvtr = new double[v.getColumnDimension()];
      for (int i = 0; i < Math.min(v.getColumnDimension(), v.getRowDimension()) && S.get(i, i) != 0.0; i++) {
        for (int k = 0; k < v.getRowDimension(); k++) {
          VTvtr[i] += VT.get(i,k) * v.get(k, i);
        }
        double predicted = Math.abs(VTvtr[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          evaluators[index][j].evaluate(expected, predicted);
        }
        predicted = Math.abs(USTUSp[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          evaluators[index][j].evaluate(expected, predicted / (S.get(i, i)*S.get(i, i)));
        }
      }
    } else {
      for (int j = 0; j < evaluators[index].length; j++) {
        evaluators[index][j].evaluate(expected, 0.0);
      }
    }
    push(pid, index);
  }
  
  public void setEvalSet(InstanceHolder evalSet) {
    lock.lock();
    if (ResultAggregator.evalSet == evalSet) {
      lock.unlock();
      return;
    }
    ResultAggregator.evalSet = evalSet;
    Matrix M = new Matrix(evalSet.size(), evalSet.getNumberOfFeatures());
    for (int i = 0; i < evalSet.size(); i++) {
      for (VectorEntry e : evalSet.getInstance(i)) {
        M.set(i, e.index, e.value);
      }
    }
    SingularValueDecomposition svd = new SingularValueDecomposition(M);
    UST = svd.getU().mul(svd.getS()).transpose();
    VT = svd.getV().transpose();
    S = svd.getS();
    lock.unlock();
    printProps();
  }
  
  public void setEvalSet(Matrix U, Matrix V, Matrix S) {
    lock.lock();
    if (LowRankResultAggregator.S == S) {
      lock.unlock();
      return;
    }
    LowRankResultAggregator.UST = U.mul(S).transpose();
    LowRankResultAggregator.VT = V.transpose();
    LowRankResultAggregator.S = S;
    lock.unlock();
    printProps();
  }
  
  private void printProps() {
    double[] arr = new double[S.getRowDimension()];
    double[] perc = new double[S.getRowDimension()];
    double sum = 0.0;
    for (int i = 0; i < S.getRowDimension(); i++) {
      arr[i] = S.get(i, i);
      sum += arr[i];
    }
    double sum2 = 0.0;
    for (int i = 0; i < S.getRowDimension(); i++) {
      sum2 += arr[i];
      perc[i] = sum2 / sum;
    }
    System.out.println("#Eigenvalues: " + Arrays.toString(arr));
    System.out.println("#Information: " + Arrays.toString(perc));
    
    //System.out.println(Arrays.toString(VT.getRow(0)));
    //System.out.println(Arrays.toString(UST.getRow(0)));
  }
  
}
