package gossipLearning.main;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.models.factorization.LowRankDecomposition;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.io.File;
import java.io.FileInputStream;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

/**
 * Main file for running algorithms in centralized way.</br>
 * The name of the configuration file is required as the 1st argument.
 * The model building is based on this file, the results are printed to 
 * the standard output.
 * 
 * @author István Hegedűs
 */
public class RecSysRun {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: RecSysRun ConfigRecSys");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", Utils.getSeed());
    System.err.println("\tRandom seed: " + seed);
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    String[] modelNames = Configuration.getNames("learner");
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    int evalTime = 1;
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, new FileInputStream(tFile), new FileInputStream(eFile));
    int[] sampleIndices = new int[reader.getTrainingSet().size()];
    for (int i = 0; i < sampleIndices.length; i++) {
      sampleIndices[i] = i;
    }
    
    // create models
    LowRankDecomposition[] models = new LowRankDecomposition[modelNames.length];
    double[][][] userModels = new double[modelNames.length][reader.getTrainingSet().size()][];
    for (int i = 0; i < modelNames.length; i++) {
      models[i] = (LowRankDecomposition)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
    }
    
    // initialize evaluator
    RecSysResultAggregator resultAggregator = new RecSysResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learning.");
    SparseVector instance;
    
    for (int iter = 0; iter <= numIters; iter++) {
      if (iter % evalTime == 0) {
        // evaluate
        for (int i = 0; i < models.length; i++) {
          for (int j = 0; j < reader.getTrainingSet().size(); j++) {
            resultAggregator.push(-1, i, j, userModels[i][j], models[i]);
          }
        }
        
        // print results
        for (AggregationResult result : resultAggregator) {
          if (iter == 0) {
            System.out.println("#iter\t" + result.getNames());
          }
          System.out.println(iter + "\t" + result);
        }
      }
      if (iter == evalTime * 10) {
        evalTime *= 10;
      }
      
      // training
      Utils.arrayShuffle(CommonState.r, sampleIndices);
      for (int i = 0; i < sampleIndices.length; i++) {
        int instanceIndex = sampleIndices[i];
        instance = reader.getTrainingSet().getInstance(instanceIndex);
        for (int j = 0; j < models.length; j++) {
          userModels[j][instanceIndex] = models[j].update(userModels[j][instanceIndex], instance);
        }
      }
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < models.length; i++) {
      for (int j = 0; j < reader.getTrainingSet().size(); j++) {
        resultAggregator.push(-1, i, j, userModels[i][j], models[i]);
      }
    }
    System.err.println(resultAggregator);
  }

}
