package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.controls.observers.errorComputation.AbstractErrorComputator;
import gossipLearning.controls.observers.errorComputation.ErrorComputator;
import gossipLearning.controls.observers.errorComputation.ErrorFunction;
import gossipLearning.controls.observers.errorComputation.ZeroOneError;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.modelHolders.BoundedModelHolder;
import gossipLearning.models.boosting.FilterBoost;
import gossipLearning.models.boosting.MergeableFilterBoost;

import java.io.File;
import java.util.Map;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;


public class MainBoost {
  public static void main(String[] args) throws Exception {
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String modelName = Configuration.getString("learner");
    long seed = Configuration.getLong("SEED");
    Random r = new Random(seed);
    int numIters = Configuration.getInt("ITER");
    
    DataBaseReader reader = DataBaseReader.createDataBaseReader(tFile, eFile);
    
    ErrorFunction errorFunction = new ZeroOneError();
    AbstractErrorComputator errorComputator = new ErrorComputator(reader.getEvalSet(), errorFunction);
    
    ModelHolder modelHolder = new BoundedModelHolder(1);
    
    // number of models
    int numOfModels = 1 << 13;
    
    Model[] models= new Model[numOfModels];
    Model model = (Model)Class.forName(modelName).newInstance();
    model.init("learner");
    model.setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    for (int i = 0; i < numOfModels; i++) {
      models[i] = (Model)model.clone();
    }
    
    System.out.println("#iter\t" + modelName + "(merged)\t" + modelName + "(simple)");
    int boostIter = 0;
    int numOfModelToUpdate = 1;
    int prevT = -1;
    int[] indices = new int[numOfModels];
    for (int index = 0; index < numOfModels; index++) {
      indices[index] = index;
    }
    for(int iter = 0, i = numOfModels; boostIter < 200 && iter < numIters && prevT < ((FilterBoost)models[indices[0]]).getT(); iter++) {
      
      for (int index = 0; index < ((i == 0) ? 1 : i); index++){
        //System.out.println("UPDATE: " + indices[index]);
        int rIndex = r.nextInt(reader.getTrainingSet().size());
        Map<Integer, Double> instance = reader.getTrainingSet().getInstance(rIndex);
        double label = reader.getTrainingSet().getLabel(rIndex);
        models[indices[index]].update(instance, label);
      }
      for (int index = Math.max(numOfModels - numOfModelToUpdate, i); index < numOfModels; index++){
        //System.out.println("UPDATE: " + indices[index]);
        int rIndex = r.nextInt(reader.getTrainingSet().size());
        Map<Integer, Double> instance = reader.getTrainingSet().getInstance(rIndex);
        double label = reader.getTrainingSet().getLabel(rIndex);
        models[indices[index]].update(instance, label);
      }
      
      if (((FilterBoost)models[indices[0]]).getSmallT() != prevT) {
        prevT = ((FilterBoost)models[indices[0]]).getSmallT();
        boostIter ++;
        //Utils.arraySuffle(r, indices, 0, i*2);
        
        double err_merge = 0.0;
        i /= 2;
        for (int index = 0; index < ((i == 0) ? 1 : i); index++){
          if (i > 0) {
            /*System.out.println("MERGE: " + indices[index] 
                + "(" + ((MergeableFilterBoost)models[indices[index]]).getSmallT() + ")"
                + "-" + indices[index + i]
                + "(" + ((MergeableFilterBoost)models[indices[index + i]]).getSmallT() + ")");*/
            ((MergeableFilterBoost)models[indices[index]]).merge((MergeableFilterBoost)models[indices[index + i]]);
            //System.out.println("AFTERMERGE: " + ((MergeableFilterBoost)models[indices[index]]).getSmallT());
          }
          modelHolder.add(models[indices[index]]);
          err_merge += errorComputator.computeError(modelHolder)[0];
        }
        err_merge /= (i == 0) ? 1 : i;
        
        double err_simple = 0.0;
        for (int index = numOfModels - numOfModelToUpdate; index < numOfModels; index++){
          modelHolder.add(models[indices[index]]);
          err_simple += errorComputator.computeError(modelHolder)[0];
        }
        err_simple /= numOfModelToUpdate;
        System.out.println(boostIter + "\t" + err_merge + "\t" + err_simple);
      }
      
      
    }
  }

}
