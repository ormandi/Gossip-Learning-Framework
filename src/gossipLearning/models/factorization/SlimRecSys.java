package gossipLearning.models.factorization;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;



public class SlimRecSys extends MergeableRecSys implements SlimModel {
  private static final long serialVersionUID = -2675295901691742814L;
  private static final String PAR_SIZE = "size";
  
  protected final double modelSize;
  protected final Set<Integer> indices;
  //protected final int[] ind;
  //protected int indSize;
  
  public SlimRecSys(String prefix) {
    super(prefix);
    modelSize = Configuration.getDouble(prefix + "." + PAR_SIZE);
    indices = new TreeSet<Integer>();
  }
  
  public SlimRecSys(SlimRecSys a) {
    super(a);
    this.modelSize = a.modelSize;
    indices = new TreeSet<Integer>(a.indices);
  }
  
  public Object clone() {
    return new SlimRecSys(this);
  }
  
  @Override
  public double[] update(double[] rowModel, SparseVector instance) {
    indices.clear();
    for (VectorEntry entry : instance) {
      indices.add(entry.index);
    }
    return super.update(rowModel, instance);
  }
  
  @Override
  public SlimRecSys getModelPart() {
    // TODO: exactly 10% send!!!
    SlimRecSys result = new SlimRecSys(this);
    for (int i = 0; i < dimension; i++) {
      if (!indices.contains(i) && modelSize < CommonState.r.nextDouble()) {
        result.columnModels[i] = null;
      }
    }
    return result;
  }

  @Override
  public Model weightedAdd(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }

}
