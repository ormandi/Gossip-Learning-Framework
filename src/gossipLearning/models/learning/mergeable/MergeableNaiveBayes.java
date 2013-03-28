package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.models.learning.multiclass.SimpleNaiveBayes;
import gossipLearning.utils.SparseVector;

public class MergeableNaiveBayes extends SimpleNaiveBayes  implements Mergeable<MergeableNaiveBayes> {
  private static final long serialVersionUID = 2340506780082847579L;
  
  public MergeableNaiveBayes() {
    super();
  }
  
  public MergeableNaiveBayes(MergeableNaiveBayes a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableNaiveBayes(this);
  }

  @Override
  public MergeableNaiveBayes merge(MergeableNaiveBayes model) {
    age = (age + model.age) * 0.5;
    maxIndex = Math.max(maxIndex, model.maxIndex);
    for (int i = 0; i < model.counts.size(); i++) {
      if (i < counts.size()) {
        /*counts.set(i, (counts.get(i) + model.counts.get(i)) * 0.5);
        mus.set(i, mus.get(i).add(model.mus.get(i)).mul(0.5));
        sigmas.set(i, sigmas.get(i).add(model.sigmas.get(i)).mul(0.5));*/
        counts.set(i, (1.0 - 1.0/age) * counts.get(i) + (1.0 / age) * model.counts.get(i));
        mus.set(i, mus.get(i).mul((1.0 - 1.0/age)).add(model.mus.get(i).mul(1.0 / age)));
        sigmas.set(i, sigmas.get(i).mul((1.0 - 1.0/age)).add(model.sigmas.get(i).mul(1.0 / age)));
      } else {
        counts.add(model.counts.get(i) * 0.5);
        mus.add((SparseVector)model.mus.get(i).mul(0.5).clone());
        sigmas.add((SparseVector)model.sigmas.get(i).mul(0.5).clone());
      }
    }
    return this;
  }

}
