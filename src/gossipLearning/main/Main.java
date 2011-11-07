package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.interfaces.Model;

import java.io.File;
import java.util.Map;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;


public class Main {
  public static void main(String[] args) throws Exception {
    Configuration.setConfig(new ParsedProperties("../res/config/iris_local.txt"));
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));;
    Random r = new Random(Configuration.getLong("SEED"));
    int numIters = Configuration.getInt("ITER");
    
    DataBaseReader reader = DataBaseReader.createDataBaseReader(tFile, eFile);
    Model model = (Model)Class.forName(Configuration.getString("learner")).newInstance();
    model.init("learner");
    model.setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
    
    Map<Integer, Double> instance;
    double label;
    for (int iter = 0; iter < numIters; iter++) {
      
      // training
      int instanceIndex = r.nextInt(reader.getTrainingSet().size());
      instance = reader.getTrainingSet().getInstance(instanceIndex);
      label = reader.getTrainingSet().getLabel(instanceIndex);
      model.update(instance, label);
      
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
      System.out.println("Iter: " + iter + "\tErr: " + err);
    }
  }

}
