package gossipLearning.models.learning.mergeable.compressible;

import gossipLearning.models.learning.mergeable.MergeableOvsA;
import gossipLearning.interfaces.models.CompressibleModel;
import java.util.*;

/**
 * A one-vs-all meta-classifier that contains CompressibleModels.
 */
public class CompressibleOvsA extends MergeableOvsA implements CompressibleModel {
  
  public CompressibleOvsA(String prefix) {
    super(prefix);
  }
  
  /** Copy constructor for deep copy. */
  public CompressibleOvsA(CompressibleOvsA a) {
    super(a);
  }
  
  @Override
  public CompressibleOvsA clone() {
    return new CompressibleOvsA(this);
  }

  @Override
  public CompressibleOvsA getModelPart() {
    return new CompressibleOvsA(this);
  }
  
  @Override
  public void getData(Map<Integer,Double> map) {
    for (int i=0; i<numberOfClasses; i++) {
      Map<Integer,Double> m = new TreeMap<Integer,Double>();
      ((CompressibleModel)classifiers.getModel(i)).getData(m);
      for (Map.Entry<Integer,Double> e : m.entrySet()) {
        if (e.getKey()<0||e.getKey()>=Integer.MAX_VALUE/numberOfClasses)
          throw new IndexOutOfBoundsException();
        map.put(e.getKey()*numberOfClasses+i,e.getValue());
      }
    }
  }
  
  @Override
  public void setData(Map<Integer,Double> map) {
    @SuppressWarnings("unchecked")
    Map<Integer,Double>[] m = new Map[numberOfClasses];
    for (int i=0; i<numberOfClasses; i++)
      m[i] = new TreeMap<Integer,Double>();
    for (Map.Entry<Integer,Double> e : map.entrySet())
      m[e.getKey()%numberOfClasses].put(e.getKey()/numberOfClasses,e.getValue());
    for (int i=0; i<numberOfClasses; i++)
      ((CompressibleModel)classifiers.getModel(i)).setData(m[i]);
  }
  
}
