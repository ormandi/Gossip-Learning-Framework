package gossipLearning.main;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.models.factorization.LowRankDecomposition;
import gossipLearning.models.factorization.PrivateRecSys;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.io.File;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class RecSysDelayed {
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
    System.err.println("\tnumber of iterations on the training set: " + numIters);
    long seed = Configuration.getLong("SEED", System.currentTimeMillis());
    System.err.println("\trandom seed: " + seed);
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    String[] modelNames = Configuration.getString("learners").split(",");
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    
    String algType = Configuration.getString("METHOD");
    System.err.println("\tPrivacy method: " + algType);
    double eps = Configuration.getDouble("EPSILON");
    System.err.println("\tPrivacy eps: " + eps);
    int T = Configuration.getInt("BUDGETITER");
    boolean isDelayed = Configuration.getBoolean("DELAYED");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    int[] sampleIndices = new int[reader.getTrainingSet().size()];
    for (int i = 0; i < sampleIndices.length; i++) {
      sampleIndices[i] = i;
    }
    
    // create models
    int logSize = (int)Math.round(Math.log(reader.getTrainingSet().size()));
    LowRankDecomposition[][] models = new LowRankDecomposition[modelNames.length][logSize];
    SparseVector userModels[][] = new SparseVector[modelNames.length][reader.getTrainingSet().size()];
    for (int i = 0; i < modelNames.length; i++) {
      models[i][0] = (LowRankDecomposition)Class.forName(modelNames[i]).getConstructor(String.class).newInstance("learners");
      for (int j = 1; j < logSize; j++) {
        models[i][j] = (LowRankDecomposition)models[i][0].clone();
      }
    }
    
    // initialize evaluator
    RecSysResultAggregator resultAggregator = new RecSysResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // learning
    System.err.println("Start learning.");
    SparseVector instance;
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor extractor = new DummyExtractor("");
    int instanceIndex = 0;
    int[] numUse = new int[reader.getTrainingSet().size()];
    
    int evalTime = 1;
    //int numThreads = 1;
    //Worker[] workers = new Worker[numThreads];
    //int batchSize = sampleIndices.length / numThreads;
    long time = System.currentTimeMillis();
    for (int iter = 0; iter <= numIters; iter++) {
      // evaluate
      if (iter % evalTime == 0) {
        for (int modelId = 0; modelId < models.length; modelId++) {
          for (int userId = 0; userId < sampleIndices.length; userId++) {
            if (!isDelayed || userId == instanceIndex) {
              modelHolder.add(models[modelId][0]);
            } else {
              modelHolder.add(models[modelId][logSize -1]);
            }
            resultAggregator.push(-1, modelId, userId, userModels[modelId][userId], modelHolder, extractor);
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
      if (iter == evalTime * 100) {// && evalTime < reader.getTrainingSet().size() / 100) {
        evalTime *= 10;
      }
      
      // training
      if (iter % sampleIndices.length == 0) {
        Utils.arrayShuffle(CommonState.r, sampleIndices);
      }
      instanceIndex = sampleIndices[iter % sampleIndices.length];
      numUse[instanceIndex] ++;
      double budgetProp = 0.0;
      if (algType.equals("fixiter")) {
        budgetProp = numUse[instanceIndex] > T ? 0.0 : 1.0 / T;
      } else if (algType.equals("halfing")) {
        budgetProp = 1.0 / (1l << numUse[instanceIndex]);
      }
      if (!isDelayed) {
        // regular update
        instance = reader.getTrainingSet().getInstance(instanceIndex);
        for (int modelId = 0; modelId < models.length; modelId++) {
          if (models[modelId][0] instanceof PrivateRecSys) {
            if (budgetProp == 0.0) {
              continue;
            }
            userModels[modelId][instanceIndex] = ((PrivateRecSys)models[modelId][0]).update(instanceIndex, userModels[modelId][instanceIndex], instance, budgetProp, eps, CommonState.r);
          } else {
            userModels[modelId][instanceIndex] = models[modelId][0].update(instanceIndex, userModels[modelId][instanceIndex], instance);
          }
        }
      } else {
        // update by log delay
        
        /*for (int i = 0; i < numThreads; i++) {
          int from = i * batchSize;
          int to = (i+1) * batchSize;
          if (i == numThreads -1) {
            to = sampleIndices.length;
          }
          if (from <= instanceIndex && instanceIndex < to) {
            workers[i] = new Worker(userModels, models, reader, from, to, instanceIndex, logSize);
            //workers[i].start();
          } else {
            LowRankDecomposition[][] modelsClone = new LowRankDecomposition[modelNames.length][logSize];
            for (int a = 0; a < models.length; a++) {
              for (int b = 0; b < models[a].length; b++) {
                modelsClone[a][b] = (LowRankDecomposition)models[a][b].clone();
              }
            }
            workers[i] = new Worker(userModels, modelsClone, reader, from, to, instanceIndex, logSize);
            //workers[i].start();
          }
        }
        for (int i = 0; i < numThreads; i++) {
          workers[i].start();
        }
        for (int i = 0; i < numThreads; i++) {
          workers[i].join();
        }*/
        
        for (int userId = 0; userId < sampleIndices.length; userId++) {
          instance = reader.getTrainingSet().getInstance(userId);
          for (int modelId = 0; modelId < models.length; modelId++) {
            LowRankDecomposition tmpModel = userId == instanceIndex ? models[modelId][0] : models[modelId][logSize -1];
            if (models[modelId][0] instanceof PrivateRecSys) {
              if (budgetProp == 0.0) {
                continue;
              }
              // other users (!=instanceIndex) dont need to add noise nor update Y
              userModels[modelId][userId] = ((PrivateRecSys)tmpModel).update(userId, userModels[modelId][userId], instance, budgetProp, eps, CommonState.r, userId == instanceIndex);
            } else {
              userModels[modelId][userId] = tmpModel.update(userId, userModels[modelId][userId], instance, userId == instanceIndex);
            }
          }
        }
        // model stepping (log)
        for (int j = 0; j < models.length; j++) {
          for (int l = logSize -1; l > 1; l--) {
            models[j][l] = models[j][l-1];
          }
          models[j][1] = (LowRankDecomposition)models[j][0].clone();
        }
      }
    }
    System.out.println("Elapsed time: " + (System.currentTimeMillis() - time));
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < models.length; i++) {
      if (i == instanceIndex) {
        modelHolder.add(models[i][0]);
      } else {
        modelHolder.add(models[i][logSize -1]);
      }
      for (int j = 0; j < reader.getTrainingSet().size(); j++) {
        resultAggregator.push(-1, i, j, userModels[i][j], modelHolder, extractor);
      }
    }
    System.err.println(resultAggregator);
  }
}

class Worker extends Thread {
  SparseVector[][] userModels;
  LowRankDecomposition[][] models;
  DataBaseReader reader;
  int from, to;
  int instanceIndex;
  int logSize;
  
  public Worker(SparseVector[][] userModels, LowRankDecomposition[][] models, DataBaseReader reader, int from, int to, int instanceIndex, int logSize) {
    this.userModels = userModels;
    this.models = models;
    this.reader = reader;
    this.from = from;
    this.to = to;
    this.instanceIndex = instanceIndex;
    this.logSize = logSize;
  }

  @Override
  public void run() {
    for (int i = from; i < to; i++) {
      SparseVector instance = reader.getTrainingSet().getInstance(i);
      for (int j = 0; j < models.length; j++) {
        LowRankDecomposition tmpModel = i == instanceIndex ? models[j][0] : (LowRankDecomposition)models[j][logSize -1].clone();
        userModels[j][i] = tmpModel.update(instanceIndex, userModels[j][i], instance);
      }
    }
  }
  
}
