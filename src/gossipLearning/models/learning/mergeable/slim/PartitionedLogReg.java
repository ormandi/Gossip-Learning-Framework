package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partitioned;
import gossipLearning.models.learning.mergeable.MergeableLogReg;
import gossipLearning.utils.*;
import peersim.config.Configuration;

/**
 * A MergeableLogReg that supports model partitioning.
 */
public class PartitionedLogReg extends MergeableLogReg implements Partitioned {
  
  /**
   * Number of partitions. (Subsampling factor.)
   * @config
   */
  private static final String PAR_NP = "numParts";
  
  protected final int numParts;
  
  protected double[] partAge;
  
  /**
   * Constructor for reading configuration parameters.
   */
  public PartitionedLogReg(String prefix) {
    super(prefix);
    numParts = Configuration.getInt(prefix + "." + PAR_NP);
    partAge = new double[numParts];
  }
  
  /**
   * Deep copy constructor.
   */
  protected PartitionedLogReg(PartitionedLogReg a) {
    super(a);
    numParts = a.numParts;
    partAge = a.partAge.clone();
  }
  
  @Override
  public PartitionedLogReg clone() {
    return new PartitionedLogReg(this);
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    for (int i=0; i<numParts; i++)
      partAge[i]++;
    
    age++;
    double lr = eta/(isTime==1?age+ageshift:1.0);
    
    gradient(instance, label);
    optimizer.delta(lr, gradient, biasGradient);
    
    for (VectorEntry entry : optimizer.delta)
      w.add(entry.index, -entry.value*(isTime==1?(age+ageshift)/(partAge[entry.index%numParts]+ageshift):1.0));
    bias -= optimizer.biasDelta;
  }
  
  @Override
  public void update(InstanceHolder instances) {
    for (int i=0; i<numParts; i++)
      partAge[i]+=instances.size();
    
    if (instances == null || instances.size() == 0) {
      return;
    }
    age += instances.size();
    double lr = eta/(isTime==1?age+ageshift:1.0);
    
    gradient(instances);
    optimizer.delta(lr, gradient, biasGradient);
    
    for (VectorEntry entry : optimizer.delta)
      w.add(entry.index, -entry.value*(isTime==1?(age+ageshift)/(partAge[entry.index%numParts]+ageshift):1.0));
    bias -= optimizer.biasDelta;
  }
  
  @Override
  public PartitionedLogReg merge(Model model) {
    PartitionedLogReg m = (PartitionedLogReg)model;
    double sum = age+m.age;
    if (sum==0)
      return this;
    double modelWeight = m.age/sum;
    age = Math.max(age,m.age);
    for (VectorEntry e : m.w) {
      int idx = e.index%numParts;
      double sum2 = partAge[idx]+m.partAge[idx];
      if (sum2==0)
        continue;
      double modelWeight2 = m.partAge[idx]/sum2;
      w.add(e.index, (e.value-w.get(e.index))*modelWeight2);
    }
    bias += (m.bias-bias)*modelWeight;
    for (int i=0; i<numParts; i++)
      partAge[i] = Math.max(partAge[i],m.partAge[i]);
    return this;
  }
  
  @Override
  public PartitionedLogReg getModelPart(int index) {
    PartitionedLogReg result = new PartitionedLogReg(this);
    result.w.clear();
    for (VectorEntry e : w)
      if (e.index%numParts==index)
        result.w.add(e.index, e.value);
    for (int i=0; i<numParts; i++)
      if (i!=index)
        result.partAge[i] = 0;
    return result;
  }
  
  @Override
  public void clear() {
    super.clear();
    for (int i=0; i<numParts; i++)
      partAge[i] = 0;
  }
  
  @Override
  public PartitionedLogReg set(Model model) {
    super.set(model);
    PartitionedLogReg m = (PartitionedLogReg)model;
    assert numParts==m.numParts;
    for (int i=0; i<numParts; i++)
      partAge[i] = m.partAge[i];
    return this;
  }

}
