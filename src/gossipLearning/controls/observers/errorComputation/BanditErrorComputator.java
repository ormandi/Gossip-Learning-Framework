package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.bandits.BanditModel;
import gossipLearning.models.bandits.GlobalArmModel;

public class BanditErrorComputator extends AbstractErrorComputator {

  public BanditErrorComputator(InstanceHolder eval, ErrorFunction errorFunction) {
    super(eval, errorFunction);
  }

  public double[] computeError(ModelHolder modelHolder) {
    double meanErrorOfNodeI = 0.0;
    BanditModel model = (BanditModel)modelHolder.getModel(modelHolder.size() -1);
    
    double predictedValue = 0.0;
    double max = 0.0;
    int maxIdx = 0;
    for (int j = 0; j < GlobalArmModel.numberOfArms(); j ++) {
      if (GlobalArmModel.getHiddenParameter(j) > max) {
        max = GlobalArmModel.getHiddenParameter(j);
        maxIdx = j;
      }
      predictedValue += model.predict(j) * model.numberOfPlayes(j);
    }
    double expectedValue = GlobalArmModel.getHiddenParameter(maxIdx) * model.numberOfAllPlayes();
    meanErrorOfNodeI = errorFunction.computeError(expectedValue, predictedValue);
    return new double[]{meanErrorOfNodeI};
  }

}
