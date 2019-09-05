package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.models.learning.multiclass.SimpleNaiveBayes;

public class MergeableNaiveBayes extends SimpleNaiveBayes  implements Mergeable {
  private static final long serialVersionUID = 2340506780082847579L;
  
  public MergeableNaiveBayes(String prefix) {
    super(prefix);
  }
  
  public MergeableNaiveBayes(MergeableNaiveBayes a) {
    super(a);
  }
  
  public MergeableNaiveBayes clone() {
    return new MergeableNaiveBayes(this);
  }

  @Override
  public Model merge(Model model) {
    MergeableNaiveBayes m = (MergeableNaiveBayes)model;
    double sum = age + m.age;
    if (sum == 0) {
      return this;
    }
    double modelWeight = m.age / sum;
    age = Math.max(age, m.age);
    
    for (int i = 0; i < numberOfClasses; i++) {
      counts[i] += (m.counts[i] - counts[i]) * modelWeight;
      mus[i].mul(1.0 - modelWeight).add(m.mus[i], modelWeight);
      sigmas[i].mul(1.0 - modelWeight).add(m.sigmas[i], modelWeight);
      /*if (counts[i] != 0.0) {
        counts[i] = (counts[i] + m.counts[i]) * 0.5;
        mus[i].mul(0.5).add(m.mus[i], 0.5);
        sigmas[i].mul(0.5).add(m.sigmas[i], 0.5);
      } else {
        counts[i] = m.counts[i];
        mus[i] = m.mus[i].clone();
        sigmas[i] = m.sigmas[i].clone();
      }*/
    }
    return this;
  }

}
