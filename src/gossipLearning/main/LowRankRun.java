package gossipLearning.main;

import gossipLearning.evaluators.FactorizationResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.models.factorization.LowRankDecomposition;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class LowRankRun {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: LowRankRun LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    long seed = Configuration.getLong("SEED");
    int evalTime = 1;
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    File tExFile = new File(Configuration.getString("trainingFileOut"));
    File eExFile = new File(Configuration.getString("evaluationFileOut"));
    
    String modelName = Configuration.getNames("extractor")[0];
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    boolean isPrintResults = Configuration.getBoolean("isPrintResults", false);
    String aggrClassName = Configuration.getString("aggrName");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    //reader.standardize();
    
    // create models
    LowRankDecomposition extractor = (LowRankDecomposition)Class.forName(Configuration.getString(modelName)).getConstructor(String.class).newInstance(modelName);
    SparseVector userModels[] = new SparseVector[reader.getTrainingSet().size()];
    
    // initialize evaluator
    FactorizationResultAggregator lrResultAggregator = (FactorizationResultAggregator)Class.forName(aggrClassName).getConstructor(String[].class, String[].class).newInstance(new String[]{modelName}, evalNames);
    lrResultAggregator.setEvalSet(reader.getTrainingSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learing.");
    SparseVector instance;
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor dummyExtractor = new DummyExtractor("");
    for (int iter = 0; iter <= numIters; iter++) {
      if (iter % evalTime == 0) {
        // evaluate
        modelHolder.add(extractor);
        for (int j = 0; j < reader.getTrainingSet().size(); j++) {
          lrResultAggregator.push(-1, 0, j, userModels[j], modelHolder, dummyExtractor);
        }
        
        // print results
        for (AggregationResult result : lrResultAggregator) {
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
      int instanceIndex = r.nextInt(reader.getTrainingSet().size());
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      userModels[instanceIndex] = extractor.update(instanceIndex, userModels[instanceIndex], instance);
      //modelHolder.add(extractor);
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final extraction result:");
    modelHolder.add(extractor);
    for (int j = 0; j < reader.getTrainingSet().size(); j++) {
      lrResultAggregator.push(-1, 0, j, userModels[j], modelHolder, dummyExtractor);
    }
    System.err.println(lrResultAggregator);
    
    // print results
    if (isPrintResults) {
      // print right eigenVectors
      double[] eigenvalues = new double[extractor.getDimension()];
      System.err.println("V-vectors: ");
      Matrix V = extractor.getV();
      System.err.println(V);
      
      // print left eigenVectors
      System.err.println("US-vectors: ");
      for (int i = 0; i < userModels.length; i++) {
        Matrix ui = extractor.getUSi(userModels[i]);
        System.err.println(ui);
        //System.err.println((i+1) + "\t" + ui);
        for (int j = 0; j < extractor.getDimension(); j++) {
          eigenvalues[j] = Utils.hypot(eigenvalues[j], ui.get(0, j));
        }
      }
      
      for (int i = 0; i < eigenvalues.length; i++) {
        eigenvalues[i] *= eigenvalues[i];
      }
      
      // print square of eigenvalues
      System.err.println("Eigenvalues^2: " + Arrays.toString(eigenvalues));
    }
    
    // extracting
    System.err.println("Extracting...");
    System.err.print("\t" + tExFile);
    InstanceHolder extracted = extractor.extract(reader.getTrainingSet());
    extracted.writeToFile(tExFile);
    System.err.println("\tdone!");
    System.err.print("\t" + eExFile);
    InstanceHolder evalSet = extractor.extract(reader.getEvalSet());
    evalSet.writeToFile(eExFile);
    System.err.println("\tdone!");
    
  }
}
