package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeablePegasos;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Random;

import peersim.config.Configuration;
import peersim.util.WeightedRandPerm;

public class SlimPegasos extends MergeablePegasos implements SlimModel, Partializable {
  private static final long serialVersionUID = 6849809999453437967L;
  
  protected static final String PAR_SIZE = "size";
  
  protected final double modelSize;
  
  public SlimPegasos(String prefix){
    super(prefix);
    modelSize = Configuration.getDouble(prefix + "." + PAR_SIZE);
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameter using the super constructor.
   * @param a model to be cloned
   */
  protected SlimPegasos(SlimPegasos a){
    super(a);
    modelSize = a.modelSize;
  }
  
  public SlimPegasos clone(){
    return new SlimPegasos(this);
  }
  
  @Override
  public Model merge(Model model) {
    SlimPegasos m = (SlimPegasos)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    for (VectorEntry e : m.w) {
      double value = w.get(e.index);
      w.add(e.index, (e.value - value) * modelWeight);
    }
    return this;
  }
  
  @Override
  public Model getModelPart(Random r) {
    SlimPegasos result = new SlimPegasos(this);
    result.w = new SparseVector(1 + (int)Math.ceil(Math.abs(numberOfFeatures * modelSize)));
    double[] weights = new double[numberOfFeatures];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = modelSize < 0 ? Math.abs(gradient.get(i)) + Utils.EPS : 1.0;
    }
    WeightedRandPerm rp = new WeightedRandPerm(r, weights);
    rp.reset(weights.length);
    int iter = (int)Math.floor(Math.abs(numberOfFeatures * modelSize));
    iter += r.nextDouble() < Math.abs(numberOfFeatures * modelSize) - iter ? 1 : 0;
    while (0 < iter && rp.hasNext()) {
      iter --;
      int idx = rp.next();
      result.w.add(idx, w.get(idx));
    }
    return result;
  }
  
  private double biasWeight = 0.0;
  private SparseVector weight;
  @Override
  public Model weightedAdd(Model model, double times) {
    if (weight == null) {
      weight = new SparseVector();
    } else {
      // if the w initialization is not 0 do not clean
      w.pointMul(weight);
      weight.mul(biasWeight);
    }
    super.add(model, times);
    SlimPegasos m = (SlimPegasos)model;
    biasWeight += times;
    for (VectorEntry entry : m.w) {
      weight.add(entry.index, times);
    }
    weight.mul(1.0 / biasWeight);
    w.div(weight);
    return this;
  }
  
  @Override
  public void clear() {
    super.clear();
    weight = null;
    biasWeight = 0.0;
  }
  
  @Override
  public double getSize() {
    return modelSize;
  }

}
