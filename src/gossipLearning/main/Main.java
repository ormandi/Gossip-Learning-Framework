package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.interfaces.Model;
import gossipLearning.models.boosting.FilterBoost;

import java.io.File;
import java.util.Map;
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
    
    DataBaseReader reader = DataBaseReader.createDataBaseReader(tFile, eFile);
    Model model = (Model)Class.forName(Configuration.getString("learner")).newInstance();
    //FilterBoost model = (FilterBoost)Class.forName(modelName).newInstance();
    model.init("learner");
    model.setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    
    Map<Integer, Double> instance;
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
        double err = 0.0;
        for (int i = 0; i < reader.getEvalSet().size(); i++) {
          instance = reader.getEvalSet().getInstance(i);
          label = reader.getEvalSet().getLabel(i);
          double prediction = model.predict(instance);
          if (prediction != label) {
            err ++;
          }
        }
        err /= reader.getEvalSet().size();
        if (model instanceof FilterBoost) {
          prevt = ((FilterBoost)model).getSmallT();
        } else {
          prevt ++;
        }
        System.out.println(prevt + "\t" + err);
      }
    }
  }

}
