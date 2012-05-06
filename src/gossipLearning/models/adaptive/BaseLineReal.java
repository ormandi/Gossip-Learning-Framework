package gossipLearning.models.adaptive;

import gossipLearning.DataBaseReader;

import java.io.File;

public class BaseLineReal extends BaseLine {
  
  private DataBaseReader reader;

  public BaseLineReal(long numOfEvals, double driftsPerEval, double samplesPerEval, double asyncRate, int numOfLearners, String trainName, String testName) throws Exception {
    super(numOfEvals, driftsPerEval, samplesPerEval, asyncRate, 2, 0, true, false, numOfLearners);
    reader = DataBaseReader.createDataBaseReader(new File(trainName), new File(testName));
    training = reader.getTrainingSet();
    evaluation = reader.getEvalSet();
    numOfClasses = training.getNumberOfClasses();
  }
  
  protected void changeLabels(double alpha){
    if (alpha == prevAlpha || alpha != Math.floor(alpha)) {
      return;
    }
    prevAlpha = alpha;
    
    double label;
    int nc;
    for (int i = 0; i < reader.getTrainingSet().size(); i++){
      label = reader.getTrainingSet().getLabel(i);
      nc = reader.getTrainingSet().getNumberOfClasses();
      reader.getTrainingSet().setLabel(i, ((int)label + 1.0)%nc);
    }
    for (int i = 0; i < reader.getEvalSet().size(); i++){
      label = reader.getEvalSet().getLabel(i);
      nc = reader.getTrainingSet().getNumberOfClasses();
      reader.getEvalSet().setLabel(i, ((int)label + 1.0)%nc);
    }
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 7){
      System.err.println("NumOfEvals DriftsPerEval SamplesPerEval AsyncRate NumOfLearners TrainFileName TestFileName");
      return;
    }
    int numOfEvals = Integer.parseInt(args[0]);
    double driftsPerEval = Double.parseDouble(args[1]);
    double samplesPerEval = Double.parseDouble(args[2]);
    double asyncRate = Double.parseDouble(args[3]);
    
    int numOfLearners = Integer.parseInt(args[4]);
    String trainName = args[5];
    String testName = args[6];
    
    BaseLineReal bl = new BaseLineReal(numOfEvals, driftsPerEval, samplesPerEval, asyncRate, numOfLearners, trainName, testName);
    bl.run();
  }

}
