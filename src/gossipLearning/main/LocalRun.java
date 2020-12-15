package gossipLearning.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
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
  enum NORMALIZATION {
    NONE, NORMALIZE, STANDARDIZE;
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: LocalRun ConfigLearning");
      System.exit(0);
    }
    
    long time = System.currentTimeMillis();
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(args));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", Utils.getSeed());
    System.err.println("\tRandom seed: " + seed);
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    String samplingMethod = Configuration.getString("SAMPLING", "uniform");
    System.err.println("\tSampling method: " + samplingMethod);
    int normalization = Configuration.getInt("NORMALIZATION", 0);
    System.err.println("\tNormalization method: " + NORMALIZATION.values()[normalization]);
    int epochs = Configuration.getInt("EPOCH", 1);
    System.err.println("\tBatch size: " + epochs);
    int batchSize = Configuration.getInt("BATCH", 1);
    System.err.println("\tEpoch size: " + batchSize);
    int evalTime = 1;
    
    // parse learning related parameters
    String include = Configuration.getString("include", null);
    String[] includes = include == null ? null : include.split("\\s");
    String[] modelNames = Configuration.getNames("learner");
    if (includes != null) {
      modelNames = new String[includes.length];
      for (int i = 0; i < includes.length; i++) {
        modelNames[i] = "learner." + includes[i];
      }
    }
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    
    // read database
    System.err.println("Reading data set.");
    //ZipFile zipFile = new ZipFile("res/db/spambase.zip");
    //DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, zipFile.getInputStream(zipFile.getEntry("train.dat")), zipFile.getInputStream(zipFile.getEntry("test.dat")));
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, new FileInputStream(tFile), new FileInputStream(eFile));
    System.err.println("\tsize: " + reader.getTrainingSet().size() + ", " + reader.getEvalSet().size() + " x " + reader.getTrainingSet().getNumberOfFeatures());
    //zipFile.close();
    
    // normalize database
    if (normalization == 2) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (normalization == 1) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    time = System.currentTimeMillis();
    
    // create models
    LearningModel[] models = new LearningModel[modelNames.length];
    for (int i = 0; i < modelNames.length; i++) {
      models[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      models[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learning.");
    SparseVector instance;
    double label;
    
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
          resultAggregator.push(-1, i, models[i]);
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
          models[i].update(batch, epochs, 0);
        }
        batch.clear();
      }
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < models.length; i++) {
      resultAggregator.push(-1, i, models[i]);
    }
    System.err.println(resultAggregator);
    System.err.println("ELAPSED TIME: " + (System.currentTimeMillis() - time) + "ms");
  }

}
