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
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;


public class Main {
  public static void main(String[] args) throws Exception {
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String modelName = Configuration.getString("learner");
    long seed = Configuration.getLong("SEED");
    Random r = new Random(seed);
    int numIters = Configuration.getInt("ITER");
    CommonState.r.setSeed(seed);
    
    DataBaseReader reader = DataBaseReader.createDataBaseReader("gossipLearning.DataBaseReader", tFile, eFile);
    Model model = (Model)Class.forName(Configuration.getString("learner")).newInstance();
    //FilterBoost model = (FilterBoost)Class.forName(modelName).newInstance();
    model.init("learner");
    model.setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    ModelHolder modelHolder = new BoundedModelHolder(1);
    modelHolder.add(model);
    
    ErrorFunction errorFunction = new ZeroOneError();
    AbstractErrorComputator testErrorComputator = new ErrorComputator(reader.getEvalSet(), errorFunction);
    AbstractErrorComputator trainErrorComputator = new ErrorComputator(reader.getTrainingSet(), errorFunction);
    
    SparseVector instance;
    double label;
    int prevt = -1;
    System.out.println("#iter\t" + modelName);
    for (int iter = 0; iter < numIters; iter++) {
      
      // training
      int instanceIndex = r.nextInt(reader.getTrainingSet().size());
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      label = reader.getTrainingSet().getLabel(instanceIndex);
      model.update(instance, label);
      
      if (!(model instanceof FilterBoost) || 
          ((model instanceof FilterBoost) && ((FilterBoost)model).getSmallT() != prevt)) {
        // evaluation
        double trainErr = trainErrorComputator.computeError(modelHolder)[0];
        double expTrainErr = 0.0;
        double testErr = testErrorComputator.computeError(modelHolder)[0];
        
        // evaluation
        if (model instanceof FilterBoost) {
          prevt = ((FilterBoost)model).getSmallT();
          expTrainErr = ((FilterBoost)model).getComulativeErr();
        }
        System.out.println(iter + "\t" + trainErr + "\t" + expTrainErr + "\t" + testErr);
      }
    }
  }

}
