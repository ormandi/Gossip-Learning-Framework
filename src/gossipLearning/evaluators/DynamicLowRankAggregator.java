package gossipLearning.evaluators;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Lanczos;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;

public class DynamicLowRankAggregator extends FactorizationResultAggregator {
  private static final long serialVersionUID = 2439960168219404027L;
  
  protected static Map<Integer, Matrix[]> pid2US;
  protected static Map<Integer, double[][]> pid2USTUSp;
  
  private static Matrix M, UST, VT, S;
  private static double fNorm2;
  private static int dimension;
  
  private static int[] failState;
  
  public DynamicLowRankAggregator(String[] modelNames, String[] evalNames) {
    super(modelNames, evalNames);
    pid2US = new TreeMap<Integer, Matrix[]>();
    pid2USTUSp = new TreeMap<Integer, double[][]>();
    // FIXME: get the rank of factorization...
    dimension = Configuration.getInt("DIMENSION");
  }
  
  protected DynamicLowRankAggregator(DynamicLowRankAggregator a) {
    super(a);
  }
  
  @Override
  public Object clone() {
    return new DynamicLowRankAggregator(this);
  }

  @Override
  public void push(int pid, int index, int userIdx, double[] userModel, MatrixBasedModel model, FeatureExtractor extractor) {
    lock.lock();
    // check state changes of nodes (churn)
    if (failState == null) {
      failState = new int[Network.size()];
      for (int i = 0; i < Network.size(); i++) {
        failState[i] = Fallible.DOWN;
      }
    }
    int numUpps = 0;
    int numDowns = 0;
    int onlines = 0;
    for (int i = 0; i < Network.size(); i++) {
      int state = Network.get(i).getFailState();
      if (state != failState[i]) {
        if (state == Fallible.OK) {
          numUpps ++;
        }
        if (state == Fallible.DOWN) {
          numDowns ++;
        }
      }
      if (state == Fallible.OK) {
        onlines ++;
      }
    }
    
    // reinit expected decomposition
    if (numUpps + numDowns > 0) {
      //System.out.print("->REINIT up: " + numUpps + " down: " + numDowns + " online: " + onlines + " uploads: " + numUploads);
      try {
        reinitMatrices(numUpps, numDowns, onlines);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    lock.unlock();
    
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
        tmpT = new double[modelNames.length][];
        for (int i = 0; i < tmpT.length; i++) {
          tmpT[i] = new double[UST.getRowDimension()];
        }
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
    
      // compute error of decomposition
      // The trace of the V^Tv matrix should contain only 1s.
      // V^T is the expected right eigenvectors
      // v is the computed right eigenvectors
      double[] VTvtr = new double[v.getColumnDimension()];
      for (int i = 0; i < Math.min(v.getColumnDimension(), v.getRowDimension()) && S.get(i, i) != 0.0; i++) {
        for (int k = 0; k < v.getRowDimension(); k++) {
          VTvtr[i] += VT.get(i,k) * v.get(k, i);
        }
        double predicted = Double.isNaN(Math.abs(VTvtr[i])) ? 0.0 : Math.abs(VTvtr[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          evaluators[index][j].evaluate(expected, predicted);
        }
        predicted = Double.isNaN(Math.abs(USTUSp[i])) ? 0.0 : Math.abs(USTUSp[i]);
        for (int j = 0; j < evaluators[index].length; j++) {
          evaluators[index][j].evaluate(expected, predicted / (S.get(i, i)*S.get(i, i)));
        }
      }
      //System.out.println(Arrays.toString(VTvtr));
    } else {
      System.out.println(v.getRowDimension() + "\t" + VT.getColumnDimension());
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
    M = new Matrix(evalSet.size(), evalSet.getNumberOfFeatures());
    for (int i = 0; i < evalSet.size(); i++) {
      for (VectorEntry e : evalSet.getInstance(i)) {
        M.set(i, e.index, e.value);
      }
    }
    UST = new Matrix(dimension, M.getRowDimension());
    lock.unlock();
  }
  
  private void reinitMatrices(int up, int down, int onlines) throws Exception {
    /*if (pid2US.get(2) != null) {
      System.out.println(Arrays.toString(pid2USTUSp.get(2)[0]));
    }*/
    fNorm2 = 0.0;
    Lanczos svd = new Lanczos();
    Matrix tmp = new Matrix(onlines, M.getColumnDimension());
    //System.out.println("dim: " + dimension + " " + onlines);
    int idx = 0;
    for (int i = 0; i < failState.length; i++) {
      int state = Network.get(i).getFailState();
      /*if (state != failState[i] && pid2US.get(2) != null) {
        System.out.println(i + " " + (state == Fallible.OK ? "OK" : "--") + Arrays.toString(pid2US.get(2)[0].getRow(i)));
      }*/
      // create matrix to be decomposed (online nodes)
      if (state == Fallible.OK) {
        double[] row = M.getRow(i);
        for (int ridx = 0; ridx < row.length; ridx ++) {
          fNorm2 = Utils.hypot(fNorm2, row[ridx]);
        }
        tmp.setRow(M.getRow(i), idx);
        idx ++;
        /*if (failState[i] == Fallible.DOWN) {
          for (int pid : pid2US.keySet()) {
            for (int index = 0; index < modelNames.length; index ++) {
              for (int j = 0; j < dimension; j++) {
                pid2US.get(pid)[index].set(i, j, 0.0);
                //UST.set(j, i, 0.0);
              }
            }
          }
        }*/
      }
      // reset user models of offline nodes
      /*if (state == Fallible.DOWN) {
        for (int pid : pid2US.keySet()) {
          for (int index = 0; index < modelNames.length; index ++) {
            for (int j = 0; j < dimension; j++) {
              pid2US.get(pid)[index].set(i, j, 0.0);
              //UST.set(j, i, 0.0);
            }
          }
        }
      }*/
      // store current fail state for nodes
      failState[i] = state;
    }
    fNorm2 *= fNorm2;
    // decompose current matrix
    //long time = System.currentTimeMillis();
    //SingularValueDecomposition svd_jama = new SingularValueDecomposition(tmp);
    //System.out.println("jama: " + (System.currentTimeMillis() - time));
    //time = System.currentTimeMillis();
    svd.run(tmp, dimension, CommonState.r);
    //System.out.println("lanczos: " + (System.currentTimeMillis() - time));
    boolean isSet;
    for (int k = 0; k < dimension; k++) {
      isSet = false;
      idx = 0;
      for (int i = 0; i < failState.length; i++) {
        if (failState[i] == Fallible.OK) {
          UST.set(k, i, svd.getU().get(idx, k) * svd.getS().get(k, k));
          idx ++;
          // compute current approximation error of online nodes
          for (int pid : pid2USTUSp.keySet()) {
            for (int index = 0; index < modelNames.length; index ++) {
              double value = UST.get(k, i) * pid2US.get(pid)[index].get(i, k);
              if (!isSet) {
                isSet = true;
                pid2USTUSp.get(pid)[index][k] = 0.0;
              }
              pid2USTUSp.get(pid)[index][k] += value;
            }
          }
        }
      }
    }
    S = svd.getS();
    if (pid2US.get(2) != null) {
      /*for (int i = 0; i < failState.length; i++) {
        System.out.print(failState[i] == 0 ? "OK" : "--");
        for (int k = 0; k < dimension; k++) {
          System.out.print(" " + pid2US.get(2)[0].get(i, k));
        }
        System.out.print(" -");
        for (int k = 0; k < dimension; k++) {
          System.out.print(" " + UST.get(k, i));
        }
        System.out.println();
      }*/
      //System.out.println(Arrays.toString(pid2USTUSp.get(2)[0]));
      //System.err.println(S);
    }
    if (VT != null) {
      Matrix X = VT.mul(svd.getV());
      double diff = 0.0;
      for (int d = 0; d < dimension; d++) {
        diff += Math.abs(1.0 - X.get(d, d));
      }
      diff /= dimension;
      System.out.println("#\t" + (CommonState.getTime()/Network.size()) + "\t" + up + "\t" + down + "\t" + onlines + "\t" + diff);
    } else {
      System.out.println("#iter\tup\tdown\tonline\tdiff");
      printProps();
    }
    VT = svd.getV().transpose();
    //System.err.println(S);
    //System.out.println(svd_jama.getS());
  }
  
  private static void printProps() {
    double[] arr = new double[S.getRowDimension()];
    double[] perc = new double[S.getRowDimension()];
    for (int i = 0; i < S.getRowDimension(); i++) {
      arr[i] = S.get(i, i);
    }
    double sum2 = 0.0;
    for (int i = 0; i < S.getRowDimension(); i++) {
      sum2 += arr[i] * arr[i];
      perc[i] = sum2 / fNorm2;
    }
    System.out.println("#Eigenvalues: " + Arrays.toString(arr));
    System.out.println("#Information: " + Arrays.toString(perc));
    
    //System.out.println(Arrays.toString(VT.getRow(0)));
    //System.out.println(Arrays.toString(UST.getRow(0)));
  }

}
