package gossipLearning.main;

import gossipLearning.evaluators.MAError;
import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.Lanczos;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.io.File;
import java.util.Arrays;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

/**
 * Reads a database based in the specified configuration file, decompose 
 * that and compares to its SVD decomposition, based on the cosine angles 
 * of the singular vectors. <br/>
 * The computation of the gradients can be computed in parallel way (e.g. with 
 * MapReduce).
 * @author István Hegedűs
 */
public class MRLowRank {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: MRLowRank LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    //long seed = Configuration.getLong("SEED");
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    
    int dimension = Configuration.getInt("dimension");
    double lambda = Configuration.getDouble("lambda");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    Matrix M = new Matrix(reader.getEvalSet().size(), reader.getEvalSet().getNumberOfFeatures());
    for (int i = 0; i < reader.getEvalSet().size(); i++) {
      for (VectorEntry e : reader.getEvalSet().getInstance(i)) {
        M.set(i, e.index, e.value);
      }
    }
    //SingularValueDecomposition svd = new SingularValueDecomposition(M);
    Lanczos svd = new Lanczos();
    svd.run(M, dimension, CommonState.r);
    Matrix UST = svd.getU().mul(svd.getS()).transpose();
    Matrix VT = svd.getV().transpose();
    Matrix S = svd.getS();
    
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
    
    Matrix U = new Matrix(reader.getTrainingSet().size(), dimension, 0.1);
    Matrix V = new Matrix(reader.getTrainingSet().getNumberOfFeatures(), dimension, 0.1);
    Matrix deltaU = new Matrix(reader.getTrainingSet().size(), dimension, 0.1);
    Matrix deltaV = new Matrix(reader.getTrainingSet().getNumberOfFeatures(), dimension, 0.0);
    Matrix R = new Matrix(dimension, dimension);
    
    Evaluator evaluator = new MAError();
    
    System.out.print("#iter");
    String[] names = evaluator.getNames();
    for (int i = 0; i < names.length; i++) {
      System.out.print("\t" + names[i]);
    }
    System.out.println();
    
    for (int iter = 0; iter <= numIters; iter ++) {
      // evaluate
      R.mulEquals(0.0);
      for (int j = 0; j < dimension; j++) {
        for (int i = 0; i < V.getRowDimension(); i++) {
          R.set(j, j, Utils.hypot(R.get(j, j), V.get(i, j)));
        }
      }
      evaluator.clear();
      Matrix VTV = VT.mul(V);
      Matrix USTU = UST.mul(U.mul(R));
      for (int i = 0; i < dimension; i++) {
        double pred = Math.abs(VTV.get(i, i)) / R.get(i, i);
        evaluator.evaluate(1.0, pred);
        pred = Math.abs(USTU.get(i, i)) / (S.get(i, i) * S.get(i, i));
        evaluator.evaluate(1.0, pred);
      }
      System.out.print(iter);
      double[] results = evaluator.getResults();
      for (int i = 0; i < results.length; i++) {
        System.out.format("\t%.6f", results[i]);
      }
      System.out.println();
      
      // compute delta
      // this can be parallelized
      double nu = lambda / Math.log(iter + 2);
      nu = lambda;
      deltaU.mulEquals(0.0);
      deltaV.mulEquals(0.0);
      for (int i = 0; i < U.getRowDimension(); i++) {
        SparseVector instance = reader.getTrainingSet().getInstance(i);
        for (int j = 0; j < V.getRowDimension(); j++) {
          double value = instance.get(j);
          for (int d = 0; d < dimension; d++) {
            double pred = U.get(i, d) * V.get(j, d);
            double err = value - pred;
            deltaU.set(i, d, deltaU.get(i, d) + V.get(j, d) * nu * err);
            deltaV.set(j, d, deltaV.get(j, d) + U.get(i, d) * nu * err);
            value -= pred;
          }
        }
      }
      
      // update U and V
      U.addEquals(deltaU);
      V.addEquals(deltaV);
    }
    //System.out.println(U);
  }

}
