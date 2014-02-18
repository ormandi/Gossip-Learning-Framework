package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.util.Random;

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
public class LocalRun {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: LocalRun LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    long seed = Configuration.getLong("SEED");
    int evalTime = numIters / Configuration.getInt("NUMEVALS");
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String[] modelNames = Configuration.getString("learners").split(",");
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    boolean isStandardize = Configuration.getBoolean("isStandardize", false);
    boolean isNormalize = Configuration.getBoolean("isNormalize", false);
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    if (isStandardize) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (isNormalize) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    
    // create models
    LearningModel[] models = new LearningModel[modelNames.length];
    for (int i = 0; i < modelNames.length; i++) {
      models[i] = (LearningModel)Class.forName(modelNames[i]).newInstance();
      models[i].init("learners");
      models[i].setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learing.");
    SparseVector instance;
    double label;
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor extractor = new DummyExtractor();
    
    for (int iter = 0; iter <= numIters; iter++) {
      if (iter % evalTime == 0) {
        // evaluate
        for (int i = 0; i < models.length; i++) {
          modelHolder.add(models[i]);
          resultAggregator.push(-1, i, modelHolder, extractor);
        }
        
        // print results
        for (AggregationResult result : resultAggregator) {
          if (iter == 0) {
            System.out.println("#iter\t" + result.getNames());
          }
          System.out.println(iter + "\t" + result);
        }
      }
      
      // training
      int instanceIndex = r.nextInt(reader.getTrainingSet().size());
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      label = reader.getTrainingSet().getLabel(instanceIndex);
      for (int i = 0; i < models.length; i++) {
        models[i].update(extractor.extract(instance), label);
      }
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < models.length; i++) {
      modelHolder.add(models[i]);
      resultAggregator.push(-1, i, modelHolder, extractor);
    }
    System.err.println(resultAggregator);
  }

}
