package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.PrivateModel;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.io.File;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class DiffPriv {
  
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: DiffPriv ConfigLearning");
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
    boolean isLogEval = true;
    if (Configuration.contains("NUMEVALS")) {
      isLogEval = false;
      evalTime = numIters / Configuration.getInt("NUMEVALS");
      System.err.println("\tEvaluating: at every " + evalTime + " iteration");
    } else {
      System.err.println("\tEvaluating: at logarithmic iterations");
    }
    Random r = new Random(seed);
    CommonState.r.setSeed(seed);
    String samplingMethod = Configuration.getString("SAMPLING", "uniform");
    System.err.println("\tSampling method: " + samplingMethod);
    String normalization = Configuration.getString("NORMALIZATION", "none");
    System.err.println("\tNormalization method: " + normalization);
    String algType = Configuration.getString("METHOD");
    System.err.println("\tPrivacy method: " + algType);
    double eps = Configuration.getDouble("EPSILON");
    System.err.println("\tPrivacy eps: " + eps);
    int T = Configuration.getInt("BUDGETITER");
    boolean isLocalNorm = Configuration.getBoolean("ISLOCALNORM");
    String normType = Configuration.getString("NORM");
    boolean isPerturbInput = Configuration.getBoolean("ISPERTURBINPUT");
    System.err.println("\tPrivacy type: " + (isPerturbInput ? "by input perturbation" : "by algorithm"));
    System.err.println("\tPrivacy norm: " + normType);
    
    int batchSize = Configuration.getInt("BATCHSIZE", 1);
    System.err.println("\tBatch size: " + batchSize);
    
    /*for (int i = 0; i < 10000; i++) {
      double x = Utils.nextNormal(0, 1.0, r);
      double y = Utils.nextNormal(100, 1.0, r);
      System.out.println("0 1:" + String.format("%.5f 2:%.5f", x, y));
    }
    for (int i = 0; i < 10000; i++) {
      double x = Utils.nextNormal(100, 1.0, r);
      double y = Utils.nextNormal(0, 1.0, r);
      System.out.println("1 1:" + String.format("%.5f 2:%.5f", x, y));
    }
    System.out.println("Test:");
    for (int i = 0; i < 1000; i++) {
      double x = Utils.nextNormal(0, 1.0, r);
      double y = Utils.nextNormal(100, 1.0, r);
      System.out.println("0 1:" + String.format("%.5f 2:%.5f", x, y));
    }
    for (int i = 0; i < 1000; i++) {
      double x = Utils.nextNormal(100, 1.0, r);
      double y = Utils.nextNormal(0, 1.0, r);
      System.out.println("1 1:" + String.format("%.5f 2:%.5f", x, y));
    }
    System.exit(0);*/
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
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
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    // normalize database
    if (normalization.equals("standardize")) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (normalization.equals("normalize")) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    
    // normalizing the length of the instances
    System.err.println("Normalizing instance length.");
    double maxnorm = 0.0;
    double norm = 0.0;
    for (int i = 0; i < reader.getTrainingSet().size(); i++) {
      if (normType.equals("L1")) {
        norm = reader.getTrainingSet().getInstance(i).norm1();
      } else if (normType.equals("L2")) {
        norm = reader.getTrainingSet().getInstance(i).norm();
      } else if (normType.equals("Linf")) {
        norm = reader.getTrainingSet().getInstance(i).norminf();
      } else {
        throw new RuntimeException("Not supported norm type: " + normType);
      }
      if (isLocalNorm) {
        reader.getTrainingSet().getInstance(i).mul(1.0 / norm);
      } else { 
        if (maxnorm < norm) {
          maxnorm = norm;
        }
      }
    }
    if (!isLocalNorm) {
      for (int i = 0; i < reader.getTrainingSet().size(); i++) {
        reader.getTrainingSet().getInstance(i).mul(1.0 / maxnorm);
      }
    }
    
    for (int i = 0; i < reader.getEvalSet().size(); i++) {
      if (normType.equals("L1")) {
        norm = reader.getEvalSet().getInstance(i).norm1();
      } else if (normType.equals("L2")) {
        norm = reader.getEvalSet().getInstance(i).norm();
      } else if (normType.equals("Linf")) {
        norm = reader.getEvalSet().getInstance(i).norminf();
      } else {
        throw new RuntimeException("Not supported norm type: " + normType);
      }
      if (isLocalNorm) {
        reader.getEvalSet().getInstance(i).mul(1.0 / norm);
      } else {
        reader.getEvalSet().getInstance(i).mul(1.0 / maxnorm);
      }
    }
    
    // differential privacy by input perturbation
    if (isPerturbInput) {
      System.err.println("Adding noise to the instances.");
      double sensitivity = 2.0;
      double[] noise = new double[reader.getTrainingSet().getNumberOfFeatures()];
      for (int i = 0; i < reader.getTrainingSet().size(); i++) {
        double length = 1.0;
        double lap = 1.0;
        double lapValue = 0.0;
        int rIdx = 0;
        for (int d = 0; d < noise.length; d++) {
          if (normType.equals("L1")) {
            noise[d] = Utils.nextLaplace(0.0, 1.0, r);
          } else if (normType.equals("L2")) {
            noise[d] = r.nextGaussian();
            length = Utils.hypot(length, noise[d]);
            lap = Utils.nextLaplace(0.0, 1.0, r);
          } else if (normType.equals("Linf")) {
            if (d == 0) {
              lapValue = Utils.nextLaplace(0.0, 1.0, r);
              rIdx = r.nextInt(noise.length);
            }
            if (d == rIdx) {
              noise[rIdx] = lapValue;
            } else {
              noise[d] = 0.0;
              noise[d] = r.nextDouble() * lapValue;
            }
          } else {
            throw new RuntimeException("Not supported norm type: " + normType);
          }
        }
        for (int d = 0; d < noise.length; d++) {
          noise[d] /= length / lap;
          noise[d] *= sensitivity / eps;
        }
        //System.out.println(Arrays.toString(noise));
        if (eps > 0) {
          reader.getTrainingSet().getInstance(i).add(noise);
        }
      }
    }
    
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
    
    int[] numUse = new int[reader.getTrainingSet().size()];
    
    // learning
    System.err.println("Start learning.");
    SparseVector instance;
    double label;
    FeatureExtractor extractor = new DummyExtractor("");
    
    int[] sampleIndices = null;
    if (samplingMethod.equals("iterative")) {
      sampleIndices = new int[reader.getTrainingSet().size()];
      for (int i = 0; i < sampleIndices.length; i++) {
        sampleIndices[i] = i;
      }
    }
    
    InstanceHolder instances = new InstanceHolder(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    for (int iter = 0; iter <= numIters; iter++) {
      if (sampleIndices != null && iter % sampleIndices.length == 0) {
        Utils.arrayShuffle(r, sampleIndices);
      }
      if (iter % evalTime == 0) {
        // evaluate
        for (int i = 0; i < models.length; i++) {
          resultAggregator.push(-1, i, models[i], extractor);
        }
        
        // print results
        for (AggregationResult result : resultAggregator) {
          if (iter == 0) {
            System.out.println("#iter\t" + result.getNames());
          }
          System.out.println(iter + "\t" + result);
        }
      }
      if (isLogEval && iter == evalTime * 10) {// && evalTime < reader.getTrainingSet().size() / 100) {
        evalTime *= 10;
      }
      
      if (iter == numIters) {
        break;
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
      
      instances.add(instance, label);
      
      // number of usages
      numUse[instanceIndex] ++;
      double budgetProp = 0.0;
      if (algType.equals("fixiter")) {
        budgetProp = numUse[instanceIndex] > T ? 0.0 : 1.0 / T;
      } else if (algType.equals("halfing")) {
        budgetProp = 1.0 / (1l << numUse[instanceIndex]);
      }
      if (instances.size() % batchSize == 0) {
        for (int i = 0; i < models.length; i++) {
          if (models[i] instanceof PrivateModel) {
            if (budgetProp == 0.0) {
              continue;
            }
            if (eps == 0) {
              models[i].update(extractor.extract(instances));
            } else {
              ((PrivateModel)models[i]).update(extractor.extract(instances), budgetProp, eps, CommonState.r);
            }
          } else {
            models[i].update(extractor.extract(instances));
          }
        }
        instances.clear();
      }
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < models.length; i++) {
      resultAggregator.push(-1, i, models[i], extractor);
    }
    System.err.println(resultAggregator);
  }

}
