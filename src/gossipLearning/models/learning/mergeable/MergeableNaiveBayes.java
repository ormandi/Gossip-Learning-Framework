package gossipLearning.models.learning.mergeable;

import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.models.learning.multiclass.SimpleNaiveBayes;
import gossipLearning.utils.SparseVector;

import java.util.Set;

public class MergeableNaiveBayes extends SimpleNaiveBayes  implements Mergeable<MergeableNaiveBayes>, Partializable<MergeableNaiveBayes> {
  private static final long serialVersionUID = 2340506780082847579L;
  
  public MergeableNaiveBayes() {
    super();
  }
  
  public MergeableNaiveBayes(MergeableNaiveBayes a) {
    super(a);
  }
  
  protected MergeableNaiveBayes(SparseVector[] mus, SparseVector[] sigmas, 
      double[] counts, double age, int numberOfClasses, int maxIndex) {
    super(mus, sigmas, counts, age, numberOfClasses, maxIndex);
  }
  
  public Object clone() {
    return new MergeableNaiveBayes(this);
  }

  @Override
  public MergeableNaiveBayes merge(MergeableNaiveBayes model) {
    age = (age + model.age) * 0.5;
    maxIndex = Math.max(maxIndex, model.maxIndex);
    for (int i = 0; i < numberOfClasses; i++) {
      if (counts[i] != 0.0) {
        counts[i] = (counts[i] + model.counts[i]) * 0.5;
        mus[i].mul(0.5).add(model.mus[i], 0.5);
        sigmas[i].mul(0.5).add(model.sigmas[i], 0.5);
      } else {
        counts[i] = model.counts[i];
        mus[i] = (SparseVector)model.mus[i].clone();
        sigmas[i] = (SparseVector)model.sigmas[i].clone();
      }
    }
    return this;
  }

  @Override
  public MergeableNaiveBayes getModelPart(Set<Integer> indices) {
    SparseVector[] mus = new SparseVector[numberOfClasses];
    SparseVector[] sigmas = new SparseVector[numberOfClasses];
    double[] counts = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      mus[i] = new SparseVector(indices.size());
      sigmas[i] = new SparseVector(indices.size());
      counts[i] = 0.0;
    }
    for (int index : indices) {
      for (int i = 0; i < numberOfClasses; i++) {
        mus[i].put(index, this.mus[i].get(index));
        sigmas[i].put(index, this.sigmas[i].get(index));
        counts[i] = this.counts[i];
      }
    }
    return new MergeableNaiveBayes(mus, sigmas, counts, serialVersionUID, numberOfClasses, maxIndex);
  }

}
