package gossipLearning.temp.main;

import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.VectorEntry;
import gossipLearning.utils.jama.SingularValueDecomposition;

import java.io.File;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class Power {

  public static void main(String[] args) throws Exception{
    if (args.length != 1) {
      System.err.println("Using: Power LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    long seed = Configuration.getLong("SEED");
    int dimension = Configuration.getInt("DIMENSION");
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    Matrix A = new Matrix(reader.getTrainingSet().size(), reader.getTrainingSet().getNumberOfFeatures());
    for (int i = 0; i < reader.getTrainingSet().size(); i++) {
      for (VectorEntry e : reader.getTrainingSet().getInstance(i)) {
        A.set(i, e.index, e.value);
      }
    }
    Matrix At = new Matrix(A).transpose();
    
    Matrix[] U = new Matrix[dimension];
    Matrix[] V = new Matrix[dimension];
    double[] un = new double[dimension];
    double[] vn = new double[dimension];
    long time = System.currentTimeMillis();
    SingularValueDecomposition svd = new SingularValueDecomposition(A);
    System.out.println("SVD time: " + (System.currentTimeMillis() - time) + "ms");
    Matrix svdUt = svd.getU().transpose();
    Matrix svdVt = svd.getV().transpose();
    //Matrix svdS = svd.getS();
    
    for (int d = 0; d < dimension; d++) {
      U[d] = new Matrix(A.getRowDimension(), 1, r.nextDouble());
      V[d] = new Matrix(A.getColumnDimension(), 1, r.nextDouble());
      un[d] = U[d].norm2();
      vn[d] = V[d].norm2();
      U[d].mulEquals(1.0 / un[d]);
      V[d].mulEquals(1.0 / vn[d]);
    }
    
    Matrix tmp;
    double err;
    Matrix defl;
    Matrix deflt;
    
    for (int iter = 0; iter <= numIters; iter++) {
      defl = A.mul(At);
      deflt = At.mul(A);
      err = 0.0;
      for (int d = 0; d < dimension; d++) {
        err += 1.0 - Math.abs(svdUt.mul(U[d]).get(d, 0));
        err += 1.0 - Math.abs(svdVt.mul(V[d]).get(d, 0));
        //err += Math.abs(1.0 - (un[d] / (svdS.get(d, d) * svdS.get(d, d)))); 
        //err += Math.abs(1.0 - (vn[d] / (svdS.get(d, d) * svdS.get(d, d))));
        
        U[d] = defl.mul(U[d]);
        V[d] = deflt.mul(V[d]);
        
        un[d] = U[d].norm2();
        vn[d] = V[d].norm2();
        U[d].mulEquals(1.0 / un[d]);
        V[d].mulEquals(1.0 / vn[d]);
        tmp = new Matrix(U[d]);
        defl.subtractEquals(U[d].mul(tmp.transpose()).mulEquals(un[d]));
        tmp = new Matrix(V[d]);
        deflt.subtractEquals(V[d].mul(tmp.transpose()).mulEquals(vn[d]));
      }
      err /= dimension * 2.0;
      System.out.println(iter + " " + err);
    }
  }

}
