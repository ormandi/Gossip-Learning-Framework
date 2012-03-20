package gossipLearning.controls.observers.errorComputation;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.bandits.BanditModel;
import gossipLearning.models.bandits.GlobalArmModel;

public class BanditPercComputator extends BanditErrorComputator {

  public BanditPercComputator(InstanceHolder eval, ErrorFunction errorFunction) {
    super(eval, errorFunction);
  }
  
  public double[] computeError(ModelHolder modelHolder) {
    double meanErrorOfNodeI = 0.0;
    BanditModel model = (BanditModel)modelHolder.getModel(modelHolder.size() -1);
    
    double max = Double.NEGATIVE_INFINITY;
    int maxIdx = -1;
    for (int j = 0; j < GlobalArmModel.numberOfArms(); j ++) {
      if (GlobalArmModel.getHiddenParameter(j) > max) {
        max = GlobalArmModel.getHiddenParameter(j);
        maxIdx = j;
      }
    }
    meanErrorOfNodeI = errorFunction.computeError(0.0, (double)model.numberOfPlayes(maxIdx) / (double)model.numberOfAllPlayes());
    return new double[]{meanErrorOfNodeI};
  }

}
