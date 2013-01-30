package gossipLearning.main;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.models.recsys.RecSysModel;
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
public class RecSysRun {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: RecSysRun LocalConfig");
      System.exit(0);
    }
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    int numIters = Configuration.getInt("ITER");
    long seed = Configuration.getLong("SEED");
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String[] modelNames = Configuration.getString("learners").split(",");
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    RecSysModel[] models = new RecSysModel[modelNames.length];
    SparseVector userModels[][] = new SparseVector[modelNames.length][reader.getTrainingSet().size()];
    for (int i = 0; i < modelNames.length; i++) {
      models[i] = (RecSysModel)Class.forName(modelNames[i]).newInstance();
      models[i].init("learners");
    }
    RecSysResultAggregator resultAggregator = new RecSysResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    System.err.println("Start learing.");
    SparseVector instance;
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor extractor = new DummyExtractor();
    
    for (int iter = 0; iter <= numIters; iter++) {
      // evaluate
      for (int i = 0; i < models.length; i++) {
        modelHolder.add(models[i]);
        for (int j = 0; j < reader.getTrainingSet().size(); j++) {
          resultAggregator.push(-1, i, j, userModels[i][j], modelHolder, extractor);
        }
      }
      
      // print results
      for (AggregationResult result : resultAggregator) {
        if (iter == 0) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println(iter + "\t" + result);
      }
      
      // training
      int instanceIndex = r.nextInt(reader.getTrainingSet().size());
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      for (int i = 0; i < models.length; i++) {
        userModels[i][instanceIndex] = models[i].update(instance, userModels[i][instanceIndex]);
        modelHolder.add(models[i]);
      }
    }
    for (int i = 0; i < models.length; i++) {
      modelHolder.add(models[i]);
      for (int j = 0; j < reader.getTrainingSet().size(); j++) {
        resultAggregator.push(-1, i, j, userModels[i][j], modelHolder, extractor);
      }
    }
    System.out.println(resultAggregator);
  }

}
