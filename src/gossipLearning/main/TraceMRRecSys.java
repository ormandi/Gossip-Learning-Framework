package gossipLearning.main;

import gossipLearning.evaluators.RMSError;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.UserTrace;
import gossipLearning.utils.VectorEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;
import peersim.core.Fallible;

public class TraceMRRecSys {
  
  public static Vector<UserTrace> loadTrace(String fName) throws Exception {
    Vector<UserTrace> userTraces = new Vector<UserTrace>();
    
    BufferedReader br = new BufferedReader(new FileReader(fName));
    String line;
    while ((line = br.readLine()) != null) {
      if (line != null) {
        Vector<Long> sessions = new Vector<Long>();
        StringTokenizer tokens = new StringTokenizer(line);
        String username = tokens.nextToken();
        String onlineToken = tokens.nextToken();
        Boolean online = ('1' == onlineToken.charAt(0));
        Long startDate = Long.parseLong(tokens.nextToken());
        int timeZone = Integer.parseInt(tokens.nextToken());
        sessions.add(Long.parseLong(tokens.nextToken()));    
        while (tokens.hasMoreTokens()) {
          sessions.add(Long.parseLong(tokens.nextToken()));
        }
        Long[] sessArr = sessions.toArray(new Long[sessions.size()]);
        userTraces.add(new UserTrace(sessArr, online, username, timeZone, startDate));
      }
    }
    br.close();
    return userTraces;
  }
  
  public static void initStates(int[] state, long[] sessionLength, UserTrace[] userTraces, Vector<UserTrace> uts, Random r) {
    for (int i = 0; i < state.length; i++) {
      //userTraces[i] = new UserTrace(uts.get(i % uts.size()));
      userTraces[i] = new UserTrace(uts.get(r.nextInt(uts.size())));
      if (userTraces[i].isFirstOnline()) {
        state[i] = Fallible.OK;
      } else {
        state[i] = Fallible.DOWN;
      }
      sessionLength[i] = userTraces[i].nextSession();
    }
  }
  
  public static void updateStates(int[] state, long[] sessionLength, UserTrace[] userTraces, int unitsInSteps) {
    for (int i = 0; i < state.length; i ++) {
      sessionLength[i] -= unitsInSteps;
      while (sessionLength[i] <= 0L) {
        if (state[i] == Fallible.OK) {
          state[i] = Fallible.DOWN;
        } else {
          state[i] = Fallible.OK;
        }
        UserTrace ut = userTraces[i];
        long sl = 0;
        if (ut.hasMoreSession()) {
          sl = ut.nextSession();
        } else {
          ut.resetPointer();
          if (ut.isFirstOnline()) {
            state[i] = Fallible.OK;
          } else {
            state[i] = Fallible.DOWN;
          }
          sl = ut.nextSession();
        }
        sessionLength[i] += sl;
      }
    }
  }
  
  private static SparseVector initVector(int dimension, SparseVector instance) {
    double max = 0.0;
    for (int d = 0; d < instance.size(); d++) {
      if (instance.valueAt(d) > max) {
        max = instance.valueAt(d);
      }
    }
    double[] v = new double[dimension + 1];
    for (int d = 0; d < dimension; d++) {
      v[d] = CommonState.r.nextDouble() * Math.sqrt(2.0*max) / dimension;
    }
    return new SparseVector(v);
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: TraceMRRecSys LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    long seed = Configuration.getLong("SEED");
    int unitsInSteps = Configuration.getInt("UnitsInIter");
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    
    int dimension = Configuration.getInt("dimension");
    double lambda = Configuration.getDouble("lambda");
    double alpha = Configuration.getDouble("alpha");
    
    String fName = Configuration.getString("traceFile");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    // evaluator
    RMSError evaluator = new RMSError();
    
    SparseVector[] X = new SparseVector[reader.getTrainingSet().size()];
    SparseVector[] Y = new SparseVector[reader.getTrainingSet().getNumberOfFeatures()];
    SparseVector[] gradX = new SparseVector[X.length];
    SparseVector[] gradY = new SparseVector[Y.length];
    
    Vector<UserTrace> uts = loadTrace(fName);
    long[] sessionLength = new long[X.length];
    int[] state = new int[X.length];
    UserTrace[] userTraces = new UserTrace[X.length];
    initStates(state, sessionLength, userTraces, uts, CommonState.r);
    
    for (int t = 0; t <= numIters; t++) {
      // eval
      for (int i = 0; i < reader.getEvalSet().size(); i++) {
        if (state[i] != Fallible.OK) {
          continue;
        }
        for (VectorEntry e : reader.getEvalSet().getInstance(i)) {
          double pred = 0.0;
          if (X[i] != null) {
            pred = X[i].get(dimension);
          }
          if (X[i] != null && Y[e.index] != null) {
            pred = X[i].mul(Y[e.index]);
          }
          evaluator.evaluate(e.value, pred);
        }
      }
      double[] results = evaluator.getResults();
      System.out.println(String.format("%d\t%.5f\t%.5f\t%.5f\t%.5f", t, results[0], results[1], results[2], results[3]));
      
      // update
      for (int i = 0; i < reader.getTrainingSet().size(); i++) {
        if (state[i] != Fallible.OK) {
          continue;
        }
        SparseVector instance = reader.getTrainingSet().getInstance(i);
        // init Xi
        if (X[i] == null) {
          X[i] = initVector(dimension, instance);
          X[i].add(dimension, instance.sum() / instance.size());
          gradX[i] = new SparseVector(dimension + 1);
        }
        for (VectorEntry e : instance) {
          // init Yj
          if (Y[e.index] == null) {
            Y[e.index] = initVector(dimension, instance);
            Y[e.index].add(dimension, 1.0);
            gradY[e.index] = new SparseVector(dimension + 1);
          }
          
          double error = e.value - X[i].mul(Y[e.index]);
          
          gradX[i].add(Y[e.index], error);
          gradY[e.index].add(X[i], error);
        }
        
      }
      for (int i = 0; i < X.length; i++) {
        if (state[i] != Fallible.OK) {
          continue;
        }
        double bias = X[i].get(dimension);
        X[i].mul(1.0 - alpha);
        X[i].add(dimension, bias - X[i].get(dimension));
        X[i].add(gradX[i], lambda);
        gradX[i].clear();
      }
      for (int i = 0; i < Y.length; i++) {
        if (Y[i] == null) {
          continue;
        }
        Y[i].mul(1.0 - alpha);
        Y[i].add(gradY[i], lambda);
        Y[i].add(dimension, 1.0 - Y[i].get(dimension));
        gradY[i].clear();
      }
      
      updateStates(state, sessionLength, userTraces, unitsInSteps);
    }
  }
}
