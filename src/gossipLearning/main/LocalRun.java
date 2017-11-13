package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

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
      System.err.println("Using: LocalRun ConfigLearning");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", System.currentTimeMillis());
    System.err.println("\tRandom seed: " + seed);
    int evalTime = 1;
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    String samplingMethod = Configuration.getString("SAMPLING", "uniform");
    System.err.println("\tSampling method: " + samplingMethod);
    String normalization = Configuration.getString("NORMALIZATION", "none");
    System.err.println("\tNormalization method: " + normalization);
    int batchSize = Configuration.getInt("BATCHSIZE", 1);
    System.err.println("\tBatch size: " + batchSize);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    String[] modelNames = Configuration.getString("learners").split(",");
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    // normalize database
    if (normalization.equals("standardize")) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (normalization.equals("normalize")) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    
    // create models
    LearningModel[] models = new LearningModel[modelNames.length];
    for (int i = 0; i < modelNames.length; i++) {
      models[i] = (LearningModel)Class.forName(modelNames[i]).getConstructor(String.class).newInstance("learners");
      models[i].setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learning.");
    SparseVector instance;
    double label;
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor extractor = new DummyExtractor("");
    
    int[] sampleIndices = null;
    if (samplingMethod.equals("iterative")) {
      sampleIndices = new int[reader.getTrainingSet().size()];
      for (int i = 0; i < sampleIndices.length; i++) {
        sampleIndices[i] = i;
      }
    }
    
    InstanceHolder batch = new InstanceHolder(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    for (int iter = 0; iter <= numIters; iter++) {
      if (sampleIndices != null && iter % sampleIndices.length == 0) {
        Utils.arrayShuffle(r, sampleIndices);
      }
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
      if (iter == evalTime * 10) {
        evalTime *= 10;
      }
      
      // training
      int instanceIndex;
      if (sampleIndices == null) {
        instanceIndex = r.nextInt(reader.getTrainingSet().size());
      } else {
        instanceIndex = sampleIndices[iter % sampleIndices.length];
      }
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      label = reader.getTrainingSet().getLabel(instanceIndex);
      batch.add(instance, label);
      if (batch.size() == batchSize) {
        for (int i = 0; i < models.length; i++) {
          models[i].update(extractor.extract(batch));
          //models[i].update(extractor.extract(instance), label);
        }
        batch.clear();
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
