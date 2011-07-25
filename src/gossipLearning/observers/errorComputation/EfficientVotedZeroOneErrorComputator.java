package gossipLearning.observers.errorComputation;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.ModelQueueHolder;

import java.util.LinkedList;
import java.util.Vector;

import peersim.core.Network;

public class EfficientVotedZeroOneErrorComputator<I, M extends Model<I>> extends AbstractErrorComputator<I, M> {
  protected int numberOfModels = -1;

  public EfficientVotedZeroOneErrorComputator(int pid, Vector<I> instances, Vector<Double> labels) {
    super(pid, instances, labels);
  }
  
  @SuppressWarnings("unchecked")
  public double[] computeError(ModelHolder<M> mH, int nodeID) {
    if (mH instanceof ModelQueueHolder && ((ModelQueueHolder<M>)mH).getModelQueue() instanceof LinkedList) {
      LinkedList<M> modelQueue = (LinkedList<M>)((ModelQueueHolder<M>) mH).getModelQueue();
            
      double[] errors = new double[numberOfComputedErrors()];
      int[] numOfPosPreds = new int[instances.size()];
      
      //System.out.println(nodeID + ": " + modelQueue.size());
      
      // compute errors
      for (int modelIdx = modelQueue.size() - 1, errorIdx = 0; errorIdx < errors.length; modelIdx --, errorIdx ++) {
        if (modelIdx >= 0) {
          double error = 0.0;
          M model = modelQueue.get(modelIdx);
          
          for (int testIdx = 0; testIdx < instances.size(); testIdx ++) {
            I testInstance = instances.get(testIdx);
            double p = model.predict(testInstance);
            //double p_old = (Utils.innerProduct(model.getModel(), testInstance) + model.getBias() > 0.0) ? 1.0 : -1.0;
            //System.out.println(p);
            numOfPosPreds[testIdx] += (p == 1.0) ? 1 : 0;
            double pRatio = (double)numOfPosPreds[testIdx] / (errorIdx + 1);
            
            double predictedValue = (pRatio >= 0.5) ? 1.0 : -1.0;
            double expectedValue = labels.get(testIdx);
            error += (expectedValue != predictedValue) ? 1.0 : 0.0;
            
            // DEBUG
            //if (nodeID == 1) {
            //  System.out.println("e: modelIdx=" + modelIdx + ", testID=" + testIdx + ", vP=" + predictedValue + ", pRatio=" + pRatio + ", numOfPosPreds=" + numOfPosPreds[testIdx] + ", numOfModels=" + (errorIdx + 1));
            //}
          }
          error /= instances.size();
          errors[errorIdx] = error;
        } else if (errorIdx > 0) {
          errors[errorIdx] = errors[errorIdx - 1];
        }
      }
      
      return errors;
    } else {
      throw new RuntimeException("Using EfficientVotedZeroONeErrorComputator requires *ModelQueueHolder* parameter and the queue implementation must be a LinkedList!!!");
    }
  }
  
  @SuppressWarnings("unchecked")
  public int numberOfComputedErrors() {
    if (numberOfModels < 0) {
      numberOfModels = ((ModelQueueHolder<M>)Network.get(0).getProtocol(pid)).getMemorySize();
    }
    return numberOfModels;
  }

}
