package tests.gossipLearning.models.learning.multiclass;

import gossipLearning.models.learning.multiclass.ANN;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;
import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class ANNTest  extends TestCase implements Serializable {
  private static final long serialVersionUID = 2747367262713232391L;
  private static boolean isConfigured = false;
  
  private ANN ann;

  @Override
  public void setUp() {
    
    if (! isConfigured) {
      Configuration.setConfig(new ParsedProperties(new String[]{"res/config/no_failure_applying_more_learners_voting10.txt"}));
      isConfigured = true;
    }
    // initialize random
    CommonState.r.setSeed(1234567890);
  }
  
  
  
  /**
   * Tests learning
   */
  public void testLearning() {
    try {
      File tFile = new File("res/db/iris_train.dat");
      File eFile = new File("res/db/iris_eval.dat");
      
      DataBaseReader r = DataBaseReader.createDataBaseReader("gossipLearning.utils.DataBaseReader", tFile, eFile);
      InstanceHolder training = r.getTrainingSet();
      InstanceHolder eval = r.getEvalSet();
      
      // initialize learner
      ann = new ANN("protocol.learningProtocol");
      ann.setNumberOfClasses(3);
      double cost = Double.MAX_VALUE;
      
      // training
      for (int i = 0; i < 5000000; i ++) {
        int idx = CommonState.r.nextInt(training.size());
        SparseVector x = training.getInstance(idx);
        double y = training.getLabel(idx);
        
        // update
        ann.update(x, y);
        
        if ( i % 1000 == 0) {
          // compute cost on evaluation set
          double evalCost = 0.0;
          double evalError = 0.0;
          for (int j = 0; j < eval.size(); j ++) {
            SparseVector xEval = eval.getInstance(j);
            double yEval = eval.getLabel(j);
            evalCost += ann.computeCostFunction(xEval, yEval, 0.0);
            double yPred = ann.predict(xEval);
            evalError += (yEval != yPred) ? 1.0 : 0.0;
          }
          evalCost /= (double)eval.size();
          evalError /= (double)eval.size();
          
          // compute cost on training set
          double trainingCost = 0.0;
          double trainingError = 0.0;
          for (int j = 0; j < training.size(); j ++) {
            SparseVector xTraining = training.getInstance(j);
            double yTraining = training.getLabel(j);
            trainingCost += ann.computeCostFunction(xTraining, yTraining, 0.0);
            double yPred = ann.predict(xTraining);
            trainingError += (yTraining != yPred) ? 1.0 : 0.0;
          }
          trainingCost /= (double)training.size();
          trainingError /= (double)training.size();
          
          // print error
          System.out.println("" + i + "\t" +trainingCost + "\t" + trainingError + "\t" + evalCost + "\t" + evalError);
          
          // store the evaluation error
          cost = trainingCost;
          if (cost < 0.1) {
            break;
          }
        }
      }
      
      assertTrue(cost < 0.1);
    } catch (Exception ex) {
      throw new RuntimeException("Reading training or evaluation database for testing was failed!", ex);
    }
  }
  
  /**
   * Returns the current theta matrices of the specified ANN by reading the output of ann.toString() i.e. the current state of
   * the model is rebuilt based on its sting representation which helps to avoid the implementation of another getter method. 
   * 
   * @param ann ANN
   * @return state
   */
  /*
  private Vector<Matrix> thetas(ANN ann) {
    try {
      Vector<Matrix> thetas = new Vector<Matrix>();
      BufferedReader in = new BufferedReader(new StringReader(ann.toString()));
      String line = in.readLine();
      Vector<Vector<Double>> mx = new Vector<Vector<Double>>();
      while (line != null) {
        if (line.startsWith("Theta") && mx.size() > 0) {
          // create matrix
          thetas.add(new Matrix(mx));
          mx.clear();
        } else if (! line.startsWith("Theta")) {
          String[] rowS = line.split(" ");
          Vector<Double> row = new Vector<Double>();
          for (int i = 0; i < rowS.length; i ++) {
            row.add(Double.parseDouble(rowS[i]));
          }
          mx.add(row);
        }
        line = in.readLine();
      }
      if (mx.size() > 0) {
        // create matrix
        thetas.add(new Matrix(mx));
        mx.clear();
      }
      
      in.close();
      return thetas;
    } catch (Exception ex) {
      throw new RuntimeException("Error while parsing thetas! ", ex);
    }
  }
  */

}
