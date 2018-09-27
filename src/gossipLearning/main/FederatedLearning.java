package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.Federated;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.main.fedAVG.ModelUpdateTask;
import gossipLearning.main.fedAVG.TaskRunner;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Utils;

import java.io.File;
import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class FederatedLearning {
  public static void main(String args[]) throws Exception {
    long time = System.currentTimeMillis();
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    int numThreads = Configuration.getInt("THREADS", 1);
    System.err.println("Simulation runs on " + numThreads + " thread(s).");
    
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", Utils.getSeed());
    System.err.println("\tRandom seed: " + seed);
    CommonState.r.setSeed(seed);
    
    
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    int normalization = Configuration.getInt("NORMALIZATION", 0);
    System.err.println("\tNormalization method: " + LocalRun.NORMALIZATION.values()[normalization]);
    
    // number of clients
    int K = Configuration.getInt("CLIENTS");
    System.err.println("\tNumber of clients: " + K);
    // proportion of clients
    double C = Configuration.getDouble("FRACTION");
    System.err.println("\tFraction of clients: " + C);
    // number of local training passes
    int E = Configuration.getInt("EPOCHS");
    System.err.println("\tNumber of epoch: " + E);
    // local minibatch size
    int B = Configuration.getInt("BATCH");
    System.err.println("\tLocal batch size: " + B);
    // number of classes per clients
    int c = Configuration.getInt("C");
    System.err.println("\tNumber of classes per clients: " + c);
    // number of clients used in update
    int numUsedClients = Math.max(1, (int)Math.round(C*K));
    
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
    int printPrecision = Configuration.getInt("printPrecision", 4);
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    // normalize database
    if (normalization == 2) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (normalization == 1) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    
    // create models
    LearningModel[] globalModels = new LearningModel[modelNames.length];
    LearningModel[] avgModels = new LearningModel[modelNames.length];
    for (int i = 0; i < modelNames.length; i++) {
      globalModels[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      globalModels[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
      avgModels[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      avgModels[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    
    InstanceHolder instances = reader.getTrainingSet();
    
    BQModelHolder modelHolder = new BQModelHolder(1);
    FeatureExtractor extractor = new DummyExtractor("");
    
    int[] clientIndices = new int[K];
    for (int i = 0; i < K; i++) {
      clientIndices[i] = i;
    }
    
    LearningModel[] localModels = new LearningModel[K];
    InstanceHolder[] localInstances = new InstanceHolder[K];
    for (int i = 0; i < K; i++) {
      localInstances[i] = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
    }
    if (0 < c) {
      LinkedList<Integer>[] map = Utils.mapLabesToNodes(instances.getNumberOfClasses(), K, c);
      for (int i = 0; i < instances.size(); i++) {
        int clientIdx = i % K;
        clientIdx = map[(int)instances.getLabel(i)].poll();
        map[(int)instances.getLabel(i)].add(clientIdx);
        localInstances[clientIdx].add(instances.getInstance(i), instances.getLabel(i));
      }
      /*for (int i = 0; i < map.length; i++) {
        System.out.println("Label " + i + " for nodes " + map[i]);
      }
      for (int i = 0; i < K; i++) {
        System.out.println(i + "\t" + localInstances[i].size());
      }
      System.exit(0);*/
    }
    
    TaskRunner taskRunner = new TaskRunner(numThreads);
    for (int t = 0; t <= numIters; t++) {
      Utils.arrayShuffle(CommonState.r, clientIndices);
      
      // evaluate global model
      for (int m = 0; m < globalModels.length; m++) {
        modelHolder.add(globalModels[m]);
        resultAggregator.push(-1, m, modelHolder, extractor);
      }
      for (AggregationResult result : resultAggregator) {
        if (t == 0) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println(t + "\t" + result);
      }
      
      for (int m = 0; m < globalModels.length; m++) {
        // send global model to clients
        for (int i = 0; i < numUsedClients; i++) {
          int idx = clientIndices[i];
          localModels[idx] = (LearningModel)globalModels[m].clone();
        }
        
        double usedSamples = 0.0;
        for (int i = 0; i < numUsedClients; i++) {
          int idx = clientIndices[i];
          usedSamples += localInstances[idx].size();
        }
        
        /*// update local models (single thread)
        for (int i = 0; i < numUsedClients; i++) {
          int idx = clientIndices[i];
          localModels[idx].update(localInstances[idx], E, B);
        }*/
        
        // reset model collector
        avgModels[m].clear();
        
        // update local models (multi thread)
        for (int i = 0; i < numUsedClients; i++) {
          int idx = clientIndices[i];
          taskRunner.add(new ModelUpdateTask(localModels[idx], localInstances[idx], E, B));
        }
        taskRunner.run();
        
        // push updated model
        for (int i = 0; i < numUsedClients; i++) {
          int idx = clientIndices[i];
          double coef = localInstances[idx].size() / usedSamples;
          coef = (globalModels[m].getAge() + localInstances[idx].size()) / (globalModels[m].getAge() + usedSamples);
          // keep gradients only
          Model model = ((Partializable)((Federated)localModels[idx]).add(globalModels[m], -1)).getModelPart();
          // averaging updated models
          ((Federated)avgModels[m]).add(model, coef);
        }
        // update global model
        ((Federated)globalModels[m]).add(avgModels[m]);
      }
    }
    System.err.println("Final result:");
    for (int i = 0; i < globalModels.length; i++) {
      modelHolder.add(globalModels[i]);
      resultAggregator.push(-1, i, modelHolder, extractor);
    }
    System.err.println(resultAggregator);
    System.err.println("ELAPSED TIME: " + (System.currentTimeMillis() - time));
  }
}
