package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.SimpleNaiveBayes;
import gossipLearning.utils.SparseVector;

public class MergeableNaiveBayes extends SimpleNaiveBayes  implements Mergeable, Partializable {
  private static final long serialVersionUID = 2340506780082847579L;
  
  public MergeableNaiveBayes(String prefix) {
    super(prefix);
  }
  
  public MergeableNaiveBayes(MergeableNaiveBayes a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableNaiveBayes(this);
  }

  @Override
  public Model merge(Model model) {
    MergeableNaiveBayes m = (MergeableNaiveBayes)model;
    for (int i = 0; i < numberOfClasses; i++) {
      if (counts[i] != 0.0) {
        counts[i] = (counts[i] + m.counts[i]) * 0.5;
        mus[i].mul(0.5).add(m.mus[i], 0.5);
        sigmas[i].mul(0.5).add(m.sigmas[i], 0.5);
      } else {
        counts[i] = m.counts[i];
        mus[i] = (SparseVector)m.mus[i].clone();
        sigmas[i] = (SparseVector)m.sigmas[i].clone();
      }
    }
    return this;
  }
  
  @Override
  public Model add(Model model) {
    return add(model, 1.0);
  }
  
  @Override
  public Model add(Model model, double times) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeableNaiveBayes getModelPart() {
    return new MergeableNaiveBayes(this);
  }

}
