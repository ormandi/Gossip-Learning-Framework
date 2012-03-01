package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.bandits.BanditModel;
import gossipLearning.models.bandits.GlobalArmModel;
import gossipLearning.models.bandits.UCBModel;

import java.util.Map;
import java.util.TreeMap;

public class BanditErrorComputator extends AbstractErrorComputator {

  public BanditErrorComputator(InstanceHolder eval, ErrorFunction errorFunction) {
    super(eval, errorFunction);
  }

  public double[] computeError(ModelHolder modelHolder) {
    double meanErrorOfNodeI = 0.0;
    double max = 0.0;
    int maxIdx = 0;
    for (int j = 0; j < GlobalArmModel.numberOfArms(); j ++) {
      if (GlobalArmModel.getHiddenParameter(j) > max) {
        max = GlobalArmModel.getHiddenParameter(j);
        maxIdx = j;
      }
    }
    Map<Integer, Double> instance = new TreeMap<Integer, Double>();
    instance.put(maxIdx, 1.0);
    BanditModel model = (UCBModel)modelHolder.getModel(modelHolder.size() -1);
    double predictedValue = model.predict(instance) * model.numberOfPlayes(maxIdx);
    double expectedValue = GlobalArmModel.getHiddenParameter(maxIdx) * model.numberOfAllPlayes();
    meanErrorOfNodeI = errorFunction.computeError(expectedValue, predictedValue);
    return new double[]{meanErrorOfNodeI};
  }

}
